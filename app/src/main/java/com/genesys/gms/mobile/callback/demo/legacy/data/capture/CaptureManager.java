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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
            DateTimeFormat.forPattern("'ScreenCap_'yyyy-MM-dd-HH-mm-ss");
    private ImageReader mImageReader;
    private ImageListener mImageListener;
    private HandlerThread mHandlerThread = null;
    private MediaProjection mProjection;
    private VirtualDisplay mDisplay;
    private ScheduledThreadPoolExecutor mExecutor;
    private ScheduledFuture<?> mFutureTask;
    private Bitmap mLatestCapture;
    private final ReentrantLock mReaderLock = new ReentrantLock();
    private final ReentrantLock mImageLock = new ReentrantLock();
    private int mWidth;
    private int mHeight;
    private String mStorageId;
    private String mAccessCode;
    private int mCounter;
    private boolean mUploading = false;
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
        mExecutor = new ScheduledThreadPoolExecutor(1);
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
        mNotificationManager.cancel(NID_SCREEN_CAPTURE);
        stopCapture();
    }

    // On start record, initialize ImageReader with screen dimensions
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private void startCapture(int resultCode, Intent data) {
        synchronized (mReaderLock) {
            try {
                String result = createShareRequest();
                Timber.d(result);
                JSONObject jsonObject = new JSONObject(result);
                mStorageId = jsonObject.getString("_id");
                mAccessCode = jsonObject.getString("_access_code");
            } catch (JSONException e) {
                Timber.e(e, "Error parsing service start result");
            } catch (IOException e) {
                Timber.e(e, "Failed to start share-request");
            }
            // TODO: share-request errors are being ignored for development

            mHandlerThread = new HandlerThread("ImageThread");
            mHandlerThread.start();
            Handler captureHandler = new Handler(mHandlerThread.getLooper());

            DisplayMetrics displayMetrics = getDisplayMetrics();
            mWidth = displayMetrics.widthPixels / 2;
            mHeight = displayMetrics.heightPixels / 2;

            mImageReader = ImageReader.newInstance(
                    mWidth,
                    mHeight,
                    PixelFormat.RGBA_8888,
                    2
            );

            // TODO: Restructure for handling orientation changes, e.g. easy pause/resume
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
            mFutureTask = mExecutor.scheduleAtFixedRate(new UploadTask(), 250, 250, TimeUnit.MILLISECONDS);
            // NTS: Don't need the timer! Just track time in ImageReader Listener!
            // NTS: Figure out proper handlers for callbacks! When/How to spawn thread?
            // TODO: Proper error handling, remove all Async tasks in favour of EventBus
        }
    }

    private void stopCapture() {
        synchronized (mReaderLock) {
            if(mHandlerThread == null) {
                return;
            }
            mFutureTask.cancel(false);
            mDisplay.release();
            mDisplay = null;
            mProjection.stop();
            mProjection = null;
            mImageReader.close();
            mImageReader = null;
            if (mHandlerThread.quit()) {
                mHandlerThread = null;
            }
        }
    }

    /*  05/13/2015
     *  Temporary code to avoid writing whole new Retrofit interfaces for two calls
     */
    public static final MediaType FORM_ENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType OCTET_STREAM = MediaType.parse("application/octet-stream");

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
        // TODO: Use retrofit
        mUploading = true;
        try {
            RequestBody body = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"screen\"; filename=\"temp\""),
                            RequestBody.create(OCTET_STREAM, screenCapture)
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
        } finally {
            mUploading = false;
        }
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
                        .setContentText("Access Code: " + mAccessCode)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_close_white_36dp, "Stop sharing", pendingIntent)
                        .build()
        );
    }

    private final class UploadTask implements Runnable {
        @Override
        public void run() {
            if(mUploading) {
                return;
            }
            synchronized (mImageLock) {
                if(mLatestCapture != null) {
                    File file = saveImage(mLatestCapture);
                    if(file == null) {
                        return;
                    }
                    new AsyncTask<File, Void, Void>() {
                        @Override
                        protected Void doInBackground(File... params) {
                            try {
                                uploadScreenCapture(mStorageId, params[0]);
                            } catch(IOException e) {
                                Timber.e(e, "Failed to upload screen capture");
                            } finally {
                                params[0].delete();
                            }
                            return null;
                        }
                    }.execute(file);
                    mLatestCapture.recycle();
                }
                mLatestCapture = null;
            }
        }
    }

    private File saveImage(Bitmap bmp) {
        String outName = FILE_FORMAT.print(DateTime.now());
        mCounter = (mCounter + 1) % 60;
        File file = null;
        OutputStream fos = null;
        try {
            file = File.createTempFile(
                    outName,
                    String.format("%d", mCounter),
                    mContext.getCacheDir()
            );
            fos = new BufferedOutputStream(new FileOutputStream(file));
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
        } catch (IOException e) {
            Timber.e(e, "Failed to save image to cache");
            return null;
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Timber.e(e, "Failed to close FileOutputStream");
                }
            }
        }
        return file;
    }

    private final class ImageListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            try {
                Image image = null;
                synchronized (mReaderLock) {
                    if (reader != mImageReader || mImageReader == null) {
                        return;
                    }
                    image = reader.acquireLatestImage();
                }
                if(image == null) {
                    return;
                }
                Bitmap bmp = obtainBitmap(image);
                if(bmp == null) {
                    return;
                }
                image.close();
                synchronized (mImageLock) {
                    if(mLatestCapture != null) {
                        mLatestCapture.recycle();
                    }
                    mLatestCapture = bmp;
                }
            } catch(IllegalStateException e) {
                Timber.e("Y'all got a bug. Images are not being released.");
            }
        }

        private Bitmap obtainBitmap(Image image) {
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mWidth;
            Bitmap bmp = Bitmap.createBitmap(
                    mWidth+rowPadding/pixelStride,
                    mHeight,
                    Bitmap.Config.ARGB_8888
            );
            bmp.copyPixelsFromBuffer(buffer);
            return bmp;
        }
    }
}
