package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.data.async.GcmRegisterAsync;
import com.genesys.gms.mobile.callback.demo.legacy.data.async.GcmUnregisterAsync;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.NoSubscriberEvent;
import hugo.weaving.DebugLog;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stau on 30/11/2014.
 */

public class GcmManager {
    private final GoogleCloudMessaging googleCloudMessaging;
    private final EventBus bus;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private AtomicInteger idGen = new AtomicInteger();

    public final static String PROPERTY_SENDER_ID = "gcm_sender_id";
    public final static String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    NotificationManager mNotificationManager;
    // private static final long[] VIBRATE_PATTERN = {350L, 200L, 350L};
    public static final String GCM_NOTIFICATION_ID = "gcm_notification_id";

    private Object savedEvent;

    @Inject @DebugLog
    public GcmManager(GoogleCloudMessaging googleCloudMessaging, SharedPreferences sharedPreferences, @ForApplication Context context) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.bus = EventBus.getDefault();
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    @DebugLog
    public void onEvent(GcmRegisterEvent event) {
        if( !checkPlayServices() ) {
            // Google Play Services not available
            bus.post(new GcmErrorEvent(null));
            return;
        }
        String strGcmRegId = getRegistrationId();
        if(!strGcmRegId.isEmpty()) {
            String currentSenderId = sharedPreferences.getString(PROPERTY_SENDER_ID, null);
            if(event.senderId == null || event.senderId.trim().isEmpty() || currentSenderId.equals(event.senderId)) {
                bus.post(new GcmRegisterDoneEvent(strGcmRegId, currentSenderId));
            } else {
                // Re-register operation
                bus.post(new GcmUnregisterEvent(event.senderId));
            }
        } else {
            GcmRegisterAsync async = new GcmRegisterAsync(googleCloudMessaging, event.senderId);
            async.execute();
        }
    }

    @DebugLog
    public void onEvent(GcmUnregisterEvent event) {
        //Timber.i("Handling GCM unregister request: " + event.toString());
        if( !checkPlayServices() ) {
            bus.post(new GcmErrorEvent(null));
            return;
        }
        String strGcmRegId = getRegistrationId();
        if(strGcmRegId.isEmpty()) {
            if(event.strNewSenderId == null || event.strNewSenderId.isEmpty()) {
                bus.post(new GcmUnregisterDoneEvent(null));
            } else {
                // Re-register operation
                bus.post(new GcmRegisterEvent(event.strNewSenderId));
            }
        } else {
            GcmUnregisterAsync async = new GcmUnregisterAsync(googleCloudMessaging, event.strNewSenderId);
            async.execute();
        }
    }

    @DebugLog
    public void onEvent(GcmRegisterDoneEvent event) {
        storeRegistrationId(event.registrationId, event.senderId);
    }

    @DebugLog
    public void onEvent(GcmUnregisterDoneEvent event) {
        storeRegistrationId(null, null);
        if(!event.isPendingWork()) {
            return;
        }
        bus.post(new GcmRegisterEvent(event.strNewSenderId));
    }

    /**
     * NoSubscriberEvent subscription allows us to observe events for which there are
     * no subscribers. This is particularly handy for seeing if there is an
     * Activity around to handle our GCM event (in the event that the application
     * has been put into the background when our notification arrives).
     *
     * @param event Returned event due to no subscribers
     */
    @DebugLog
    public void onEvent(NoSubscriberEvent event) {
        CharSequence message = "Message received!";
        if(event.originalEvent instanceof GcmReceiveEvent) {
            savedEvent = event.originalEvent;
            CharSequence extraMessage = ((GcmReceiveEvent) savedEvent).extras.getCharSequence("message");
            if(extraMessage != null) {
                message = extraMessage;
            }
        }

        /*
        NotificationCompat.Builder builder =
            getNotificationBuilder(R.drawable.ic_launcher, context.getResources().getString(R.string.launcher_name), message);

        // int notificationId = mNotifyId.getAndIncrement();
        Intent resultIntent = new Intent(context, MainActivity.class);
        //resultIntent.putExtra(GCM_NOTIFICATION_ID, notificationId);
        resultIntent.putExtra(GCM_NOTIFICATION_ID, 0);

        // TaskStack allows us to go back to Home from the Notification action
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            );
        builder.setContentIntent(resultPendingIntent);
        //getNotificationManager().notify(notificationId, builder.build());
        /*
         * Force notification ID to 0 to prevent creating new entries in the
         * notification drawer.
         */
        //getNotificationManager().notify(0, builder.build());
    }



    protected NotificationManager getNotificationManager() {
        if(mNotificationManager==null) {
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    protected NotificationCompat.Builder getNotificationBuilder(int icon, CharSequence title, CharSequence text) {
        // TODO: Is it worthwhile to cache this at all?
        // setDefaults will default the notification vibration/sound/lights settings
        return new NotificationCompat.Builder(context)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(resultCode!= ConnectionResult.SUCCESS){
            // Timber.w("Google Play Services are not available.");
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // TODO: Publish event to indicate GooglePlayServices is not available
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId() {
        String registrationId = sharedPreferences.getString(PROPERTY_REG_ID, "");
        if(registrationId.isEmpty()) {
            // No registration found
            //Timber.d("No saved Registration ID found.");
            return "";
        }
        int registeredVersion = sharedPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if(registeredVersion!=currentVersion) {
            // Version changed
            //Timber.d("Version ID has changed and Registration ID is no longer valid.");
            return "";
        }
        return registrationId;
    }

    // TODO: Move into utility class
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //Timber.e(e, "Failed to obtain application version code.");
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Saves/Clears the GCM Registration ID in SharedPreferences.
     * Synchronized to prevent two threads from somehow simultaneously
     * mucking around with the GCM Registration ID.
     *
     * @param regId GCM Registration ID to store. Empty if clearing persisted data.
     */
    @DebugLog
    private synchronized void storeRegistrationId(String regId, String senderId) {
        int appVersion = getAppVersion();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(regId == null || regId.isEmpty()) {
            editor.remove(PROPERTY_REG_ID);
            editor.remove(PROPERTY_APP_VERSION);
        } else {
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putString(PROPERTY_SENDER_ID, senderId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
        }
        // apply() tells the editor to perform the save asynchronously.
        editor.apply();
    }
}