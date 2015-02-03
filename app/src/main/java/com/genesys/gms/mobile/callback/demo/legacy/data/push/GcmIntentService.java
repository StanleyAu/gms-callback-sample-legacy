package com.genesys.gms.mobile.callback.demo.legacy.data.push;

import android.app.IntentService;
import android.content.Intent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmReceiveEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.greenrobot.event.EventBus;

/**
 * Created by stau on 11/27/2014.
 *
 * Our IntentService's only responsibility is to produce a GcmReceiveEvent
 * onto the Otto bus for the GcmManager or MainFragment to process
 */
public class GcmIntentService extends IntentService {

    public static final String GCM_NOTIFICATION_ID = "gcm_notification_id";
    private final EventBus bus;

    public GcmIntentService() {
        super("GcmIntentService");
        bus = EventBus.getDefault();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        GcmReceiveEvent event = new GcmReceiveEvent(intent, false);
        if(event.extras != null) {
            // TODO: Handle specific GCM message types
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // Send error
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // Messages deleted on server
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Actual message to process
                // Send to Activity via Bus (attn to threading)
            }
        }
        bus.post(event);

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}