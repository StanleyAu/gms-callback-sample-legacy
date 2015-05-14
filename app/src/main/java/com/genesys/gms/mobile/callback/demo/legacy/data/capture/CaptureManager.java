package com.genesys.gms.mobile.callback.demo.legacy.data.capture;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.capture.StartCaptureEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.capture.StopCaptureEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.squareup.okhttp.*;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by stau on 5/7/2015.
 * This could've been implemented as a proper Service
 */
@Singleton
public class CaptureManager {
    public static final int CREATE_SCREEN_CAPTURE = 4242;
    private static final String DISPLAY_NAME = "Capture";
    private static final int NID_SCREEN_CAPTURE = 3;
    private static final DateTimeFormatter FILE_FORMAT =
            DateTimeFormat.forPattern("'ScreenCap_'yyyy-MM-dd-HH-mm-ss'.png'");
    private ImageReader mImageReader;
    private ImageListener mImageListener;
    private ReentrantLock mImageLock = new ReentrantLock(true);
    private HandlerThread mHandlerThread;
    private MediaProjection mProjection;
    private VirtualDisplay mDisplay;
    private int mWidth;
    private int mHeight;
    private String mAccessCode;
    private final Context mContext;
    private final WindowManager mWindowManager;
    private final MediaProjectionManager mMediaProjectManager;
    private final NotificationManagerCompat mNotificationManager;
    private final OkHttpClient mClient;
    private final GmsEndpoint mEndpoint;
    private final EventBus mBus;

    @Inject
    public CaptureManager(WindowManager windowManager, MediaProjectionManager mediaProjectionManager, NotificationManagerCompat notificationManager, OkHttpClient client, GmsEndpoint endpoint, @ForApplication Context context) {
        mContext = context;
        mWindowManager = windowManager;
        mMediaProjectManager = mediaProjectionManager;
        mNotificationManager = notificationManager;
        mClient = client;
        mEndpoint = endpoint;
        mBus = EventBus.getDefault();
        mImageListener = new ImageListener();
        mHandlerThread = null;
    }

    @SuppressWarnings("ResourceType")
    public static void fireScreenCaptureEvent(Activity activity) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        activity.startActivityForResult(intent, CREATE_SCREEN_CAPTURE);
    }

    public void onEventAsync(StartCaptureEvent event) {
        if (mHandlerThread != null) {
            Timber.d("Screen capture already running!");
            return;
        }
        if (event.resultCode == 0) {
            Timber.d("Permission for screen capture not granted.");
        } else {
            Timber.d("Starting screen capture.");
           startCapture(event.resultCode, event.data);
        }
    }

    public void onEventAsync(StopCaptureEvent event) {
        if (mHandlerThread == null) {
            Timber.d("Screen capture already stopped!");
            return;
        }
        stopCapture();
    }

    // On start record, initialize ImageReader with screen dimensions
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private void startCapture(int resultCode, Intent data) {
        mImageLock.lock();
        try {
            mHandlerThread = new HandlerThread("ImageThread");
            mHandlerThread.start();
            Handler captureHandler = new Handler(mHandlerThread.getLooper());

            DisplayMetrics displayMetrics = getDisplayMetrics();
            mWidth = displayMetrics.widthPixels;
            mHeight = displayMetrics.heightPixels;

            mImageReader = ImageReader.newInstance(
                    mWidth,
                    mHeight,
                    PixelFormat.RGBA_8888,
                    5
            );

            mProjection = mMediaProjectManager.getMediaProjection(resultCode, data);
            Surface surface = mImageReader.getSurface();
            mDisplay = mProjection.createVirtualDisplay(
                    DISPLAY_NAME,
                    mWidth,
                    mHeight,
                    displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                    surface,
                    new VirtualDisplay.Callback() {
                        @Override
                        public void onStopped() {
                            mBus.post(new StopCaptureEvent());
                        }
                    },
                    captureHandler
            );
            mImageReader.setOnImageAvailableListener(mImageListener, captureHandler);
            notifyScreenCapture();

            // NTS: Don't need the timer! Just track time in ImageReader Listener!
            // NTS: Figure out proper handlers for callbacks! When/How to spawn thread?
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String result = createShareRequest();
                        Timber.d(result);
                        JSONObject jsonObject = new JSONObject(result);
                        mAccessCode = jsonObject.getString("_access_code");
                    } catch (JSONException e) {
                        Timber.e(e, "Error parsing service start result");
                        // Errors are ignored for now
                    } catch (IOException e) {
                        Timber.e(e, "Failed to start share-request");
                    }
                    return null;
                }
            }.execute();
            // TODO: Proper error handling, remove all Async tasks in favour of EventBus
        } finally {
            mImageLock.unlock();
        }
    }

    private void stopCapture() {
        mImageLock.lock();
        try {
            mNotificationManager.cancel(NID_SCREEN_CAPTURE);
            mDisplay.release();
            mDisplay = null;
            mProjection.stop();
            mProjection = null;
            mImageReader.close();
            mImageReader = null;
            if (mHandlerThread.quit()) {
                mHandlerThread = null;
            }
        } finally {
            mImageLock.unlock();
        }
    }

    /*  05/13/2015
     *  Temporary code to avoid writing whole new Retrofit interfaces for two calls
     */
    public static final MediaType FORM_ENCODED
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType MEDIA_PNG
            = MediaType.parse("image/png");

    @DebugLog
    private String createShareRequest() throws IOException {
        RequestBody body = RequestBody.create(FORM_ENCODED, "account_id=1234");
        String strBaseUri = mEndpoint.getUrl();
        String strCreateUri = new Uri.Builder()
                .encodedPath(strBaseUri)
                .appendPath("service")
                .appendPath("share-request")
                .toString();
        Request request = new Request.Builder()
                .url(strCreateUri)
                .post(body)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    @DebugLog
    private String uploadScreenCapture(String serviceId, File screenCapture) throws IOException {
        RequestBody body = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"screen\""),
                        RequestBody.create(MEDIA_PNG, screenCapture)
                ).build();
        String strBaseUri = mEndpoint.getUrl();
        String strUploadUri = new Uri.Builder()
                .encodedPath(strBaseUri)
                .appendPath("service")
                .appendEncodedPath(serviceId)
                .appendPath("storage")
                .toString();
        Request request = new Request.Builder()
                .url(strUploadUri)
                .post(body)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }
    /*  05/13/2015
     *  End Temporary Code
     */

    private void notifyScreenCapture() {
        Intent intent = new Intent(mContext, CaptureIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // TODO: Use String resources
        mNotificationManager.notify(
                NID_SCREEN_CAPTURE,
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Screen Sharing")
                        .setContentText("Active, touch to stop")
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .build()
        );
    }

    private final class ImageListener implements ImageReader.OnImageAvailableListener {
        private DateTime lastCaptureDeliveredAt;
        public ImageListener() {
            lastCaptureDeliveredAt = DateTime.now();
        }
        @Override
        public void onImageAvailable(ImageReader reader) {
            mImageLock.lock();
            try {
                if(reader != mImageReader || mImageReader == null) {
                    return;
                }
                Timber.d("New image available for screen share!");
                Image image = reader.acquireLatestImage();
                if(!DateTime.now().isAfter(lastCaptureDeliveredAt.plusSeconds(1))) {
                    if (image != null) {
                        image.close();
                    }
                    return;
                }
                // Check time
                if(image != null) {
                    String outName = FILE_FORMAT.print(DateTime.now());
                    File file = new File(mContext.getFilesDir(), outName);
                    Bitmap bmp = null;
                    OutputStream outFile = null;
                    try {
                        outFile = new BufferedOutputStream(new FileOutputStream(file));
                        bmp = obtainBitmap(image);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, outFile);
                    } catch(IOException e) {
                        Timber.e(e, "Failed to save screen capture.");
                    } finally {
                        if(outFile != null) {
                            try {
                                outFile.close();
                            } catch (IOException e) {
                                Timber.e(e, "Failed to close FileOutputStream.");
                            }
                        }
                        if(bmp != null) {
                            bmp.recycle();
                        }
                        image.close();
                    }
                    // After everything's been closed, it's flushed to file
                    // TypedFile typedFile = new TypedFile("multipart/form-data", file);
                    // File hasn't been closed yet
                    new AsyncTask<File, Void, Void>() {
                        @Override
                        protected Void doInBackground(File... params) {
                            try {
                                uploadScreenCapture(mAccessCode, params[0]);
                            } catch(IOException e) {
                                Timber.e(e, "Failed to upload screen capture");
                            } finally {
                                params[0].delete();
                            }
                            return null;
                        }
                    }.execute(file);
                }
            } catch(IllegalStateException e) {
                Timber.e("Y'all got a bug. Images are not being released.");
            } finally {
                mImageLock.unlock();
            }
        }

        private Bitmap obtainBitmap(Image image) {
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mWidth;
            Bitmap bmp = Bitmap.createBitmap(mWidth + rowPadding/pixelStride, mHeight, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buffer);
            return bmp;
        }
    }
}
