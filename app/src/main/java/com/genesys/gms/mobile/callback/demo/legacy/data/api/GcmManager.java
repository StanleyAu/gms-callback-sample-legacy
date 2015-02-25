package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmChatMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmSyncMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm.*;
import com.genesys.gms.mobile.callback.demo.legacy.ui.GenesysChatActivity;
import com.genesys.gms.mobile.callback.demo.legacy.ui.GenesysSampleActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.NoSubscriberEvent;
import hugo.weaving.DebugLog;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Created by stau on 30/11/2014.
 */
@Singleton
public class GcmManager {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_SENDER_ID = "gcm_sender_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final int NID_CALLBACK = 1;
    private static final int NID_CHAT = 2;
    private static final int MAX_NOTED_TRANSCRIPTS = 3;

    private final GoogleCloudMessaging googleCloudMessaging;
    private final Gson gson;
    private final EventBus bus;
    private final SharedPreferences sharedPreferences;
    private final Context context;

    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.InboxStyle mInboxStyle;

    @Inject
    public GcmManager(GoogleCloudMessaging googleCloudMessaging, Gson gson, SharedPreferences sharedPreferences, @ForApplication Context context) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.gson = gson;
        this.bus = EventBus.getDefault();
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void onEventAsync(GcmRegisterEvent event) {
        if( !checkPlayServices() ) {
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

    public void onEvent(GcmRegisterDoneEvent event) {
        storeRegistrationId(event.registrationId, event.senderId);
    }

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
    public void onEvent(NoSubscriberEvent event) {
        if(!(event.originalEvent instanceof GcmReceiveEvent)) {
            Timber.d("No subscriber for event: %s", event.originalEvent);
            return;
        }

        GcmReceiveEvent gcmReceiveEvent = (GcmReceiveEvent)event.originalEvent;
        String message = gcmReceiveEvent.extras.getString("message");
        Timber.d("Unparsed GCM message: %s", message);
        try {
            GcmSyncMessage gcmSyncMessage = gson.fromJson(message, GcmSyncMessage.class);
            if(!notifyForCallback(gcmSyncMessage)) {
            }
        } catch(JsonSyntaxException e) {;}
        try {
            GcmChatMessage gcmChatMessage = gson.fromJson(message, GcmChatMessage.class);
            if(!notifyForChat(gcmChatMessage)) {
            }
        } catch(JsonSyntaxException e) {;}

        Timber.w("NoSubscriberEvent dropped: %s", event.originalEvent);
    }

    @DebugLog
    private boolean notifyForCallback(GcmSyncMessage gcmSyncMessage) {
        if(gcmSyncMessage == null || gcmSyncMessage.getAction() == null) {
            return false;
        }

        Intent intent = new Intent(context, GenesysSampleActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // TODO: Use String resources
        getNotificationManager().notify(
            NID_CALLBACK,
            new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Genesys Callback")
                .setContentText("Your attention is needed!")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
        );

        return true;
    }

    @DebugLog
    private boolean notifyForChat(GcmChatMessage gcmChatMessage) {
        if(gcmChatMessage == null || gcmChatMessage.getMessage() == null) {
            return false;
        }

        NotificationCompat.InboxStyle noteStyle = null;
        int numTranscripts = sharedPreferences.getInt("newMessages", 0);
        int addedTranscripts = numTranscripts;
        if(numTranscripts == 0) {
            mInboxStyle = null;
        }

        String firstLine = null;
        if(gcmChatMessage.getLastTranscript() != null) {
            numTranscripts += gcmChatMessage.getLastTranscript().size();

            if (numTranscripts > 0) {
                noteStyle = getInboxStyle()
                    // .setBigContentTitle() DEFAULT TO ContentTitle
                    .setSummaryText(String.format("%d new messages", numTranscripts));
                for (GcmChatMessage.TranscriptBrief transcriptBrief : gcmChatMessage.getLastTranscript()) {
                    if (firstLine == null) {
                        firstLine = transcriptBrief.getMessageText();
                    }
                    if (addedTranscripts++ >= MAX_NOTED_TRANSCRIPTS) {
                        break;
                    }
                    noteStyle.addLine(transcriptBrief.getMessageText());
                }
            }
            sharedPreferences.edit().putInt("newMessages", numTranscripts).apply();
        }

        Intent intent = new Intent(context, GenesysChatActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // TODO: Use String resources
        getNotificationManager().notify(
            NID_CHAT,
            new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(gcmChatMessage.getMessage())
                .setContentText(firstLine == null ? "Touch to view." : firstLine)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setStyle(noteStyle)
                .build()
        );

        return true;
    }

    protected NotificationManagerCompat getNotificationManager() {
        if(mNotificationManager==null) {
            mNotificationManager = NotificationManagerCompat.from(context);
        }
        return mNotificationManager;
    }

    protected NotificationCompat.InboxStyle getInboxStyle() {
        // TODO: Is it worthwhile to cache this at all?
        // setDefaults will default the notification vibration/sound/lights settings
        if(mInboxStyle == null) {
            mInboxStyle = new NotificationCompat.InboxStyle();
        }
        return mInboxStyle;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(resultCode != ConnectionResult.SUCCESS){
            Timber.w("Google Play Services are not available.");
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
            Timber.d("No saved Registration ID found.");
            return "";
        }
        int registeredVersion = sharedPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if(registeredVersion!=currentVersion) {
            // Version changed
            Timber.d("Version ID has changed and Registration ID is no longer valid.");
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
        editor.apply();
    }
}