package com.genesys.gms.mobile.callback.demo.legacy.data.capture;

import android.app.IntentService;
import android.content.Intent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.capture.StopCaptureEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by stau on 5/14/2015.
 */
public class CaptureIntentService extends IntentService {
  private final EventBus bus;

  public CaptureIntentService() {
    super("CaptureIntentService");
    bus = EventBus.getDefault();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Timber.d("Handling Notification intent for StopCaptureEvent");
    bus.post(new StopCaptureEvent());
  }
}
