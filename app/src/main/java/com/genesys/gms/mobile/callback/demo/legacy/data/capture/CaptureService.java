package com.genesys.gms.mobile.callback.demo.legacy.data.capture;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import com.genesys.gms.mobile.callback.demo.legacy.App;

/**
 * Created by stau on 5/26/2015.
 */
public class CaptureService extends Service {
  private boolean isRunning = false;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (isRunning) {
      // Tells Android OS to not recreate service if it was killed to recover memory
      return START_NOT_STICKY;
    }

    ((App) getApplication()).getApplicationGraph().inject(this);
    // Start CaptureManager

    isRunning = true;
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    // clean up screen capture
    super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Listen for orientation change
    switch (newConfig.orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:
        break;
      case Configuration.ORIENTATION_PORTRAIT:
        break;
      default:
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new AssertionError("Not used.");
  }
}
