package com.genesys.gms.mobile.callback.demo.legacy.data.push;

import android.app.IntentService;
import android.content.Intent;
import com.genesys.gms.mobile.callback.demo.legacy.App;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm.GcmReceiveEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stau on 11/27/2014.
 * <p/>
 * Our IntentService's only responsibility is to produce a GcmReceiveEvent
 * onto the Otto bus for the GcmManager or MainFragment to process
 */
public class GcmIntentService extends IntentService {
  public static final String GCM_NOTIFICATION_ID = "gcm_notification_id";
  @Inject
  GoogleCloudMessaging gcm;
  private final EventBus bus;

  public GcmIntentService() {
    super("GcmIntentService");
    bus = EventBus.getDefault();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String messageType = gcm.getMessageType(intent);

    GcmReceiveEvent event = new GcmReceiveEvent(intent, false);
    if (event.extras != null) {
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
    bus.postSticky(event);

    GcmBroadcastReceiver.completeWakefulIntent(intent);
  }

  // Dagger wiring
  private ObjectGraph serviceGraph;

  @Override
  public void onCreate() {
    super.onCreate();
    App application = (App) getApplication();
    serviceGraph = application.getApplicationGraph().plus(getModules().toArray());
    serviceGraph.inject(this);
  }

  @Override
  public void onDestroy() {
    serviceGraph = null;
    super.onDestroy();
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(new GcmModule(this));
  }
  // End Dagger wiring
}