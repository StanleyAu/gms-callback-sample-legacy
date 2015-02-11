package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmSyncMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm.*;
import com.genesys.gms.mobile.callback.demo.legacy.ui.GenesysChatActivity;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.NoSubscriberEvent;
import hugo.weaving.DebugLog;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stau on 30/11/2014.
 */
@Singleton
public class GcmManager {
    private final GoogleCloudMessaging googleCloudMessaging;
    private final Gson gson;
    private final EventBus bus;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private AtomicInteger idGen = new AtomicInteger();

    public final static String PROPERTY_SENDER_ID = "gcm_sender_id";
    public final static String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    NotificationManagerCompat mNotificationManager;
    // private static final long[] VIBRATE_PATTERN = {350L, 200L, 350L};
    public static final String GCM_NOTIFICATION_ID = "gcm_notification_id";

    @Inject @DebugLog
    public GcmManager(GoogleCloudMessaging googleCloudMessaging, Gson gson, SharedPreferences sharedPreferences, @ForApplication Context context) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.gson = gson;
        this.bus = EventBus.getDefault();
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void onEventAsync(GcmRegisterEvent event) {
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
            String result = null;
            try {
                result = googleCloudMessaging.register(event.senderId);
            } catch(IOException e) {
                bus.post(new GcmErrorEvent(e));
            }
            if (result != null && !result.isEmpty()) {
                bus.post(new GcmRegisterDoneEvent(result, event.senderId));
            } else {
                // Unknown issue
            }
        }
    }

    public void onEventAsync(GcmUnregisterEvent event) {
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
            try {
                googleCloudMessaging.unregister();
            } catch (IOException e) {
                bus.post(new GcmErrorEvent(e));
            }
            bus.post(new GcmUnregisterDoneEvent(event.strNewSenderId));
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
        if(!(event.originalEvent instanceof GcmReceiveEvent)) {
            Log.d("GcmManager", event.originalEvent.toString());
            return;
        }
        GcmReceiveEvent gcmReceiveEvent = (GcmReceiveEvent)event.originalEvent;
        String message = gcmReceiveEvent.extras.getString("message");
        GcmSyncMessage gcmSyncMessage = null;
        try {
            gcmSyncMessage = gson.fromJson(message, GcmSyncMessage.class);
        } catch(JsonSyntaxException e) {
            Log.e("GcmManager", "Unable to parse GCM message", e);
        }

        /*
        Intent resultIntent = new Intent(context, GenesysChatActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(resultIntent);
        */

        NotificationCompat.Builder builder =
            getNotificationBuilder(
                R.drawable.ic_launcher,
                context.getResources().getString(R.string.title_activity_genesys_sample),
                message
            );

        builder.setOngoing(true);
        // int notificationId = mNotifyId.getAndIncrement();
        Intent resultIntent = new Intent(context, GenesysChatActivity.class);
        //resultIntent.putExtra(GCM_NOTIFICATION_ID, notificationId);
        //resultIntent.putExtra(GCM_NOTIFICATION_ID, 0);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(GenesysChatActivity.class);
        //stackBuilder.addNextIntent(resultIntent);
        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
        //    0, PendingIntent.FLAG_UPDATE_CURRENT
        //);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        //getNotificationManager().notify(notificationId, builder.build());
        getNotificationManager().notify(0, builder.build());
    }

    protected NotificationManagerCompat getNotificationManager() {
        if(mNotificationManager==null) {
            mNotificationManager = NotificationManagerCompat.from(context);
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