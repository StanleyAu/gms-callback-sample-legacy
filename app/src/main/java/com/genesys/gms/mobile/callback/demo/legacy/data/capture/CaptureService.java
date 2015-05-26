package com.genesys.gms.mobile.callback.demo.legacy.data.capture;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import com.genesys.gms.mobile.callback.demo.legacy.App;
import dagger.ObjectGraph;

import java.util.Arrays;
import java.util.List;

/**
 * Created by stau on 5/26/2015.
 */
public class CaptureService extends Service {
  @Override
  public void onCreate() {
    super.onCreate();
    dagger_onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onDestroy() {
    // clean up screen capture
    dagger_onDestroy();
    super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Listen for orientation change
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new AssertionError("Not used.");
  }

  // Dagger wiring
  private ObjectGraph serviceGraph;

  protected void dagger_onCreate() {
    App application = (App) getApplication();
    serviceGraph = application.getApplicationGraph().plus(getModules().toArray());
    serviceGraph.inject(this);
  }

  protected void dagger_onDestroy() {
    serviceGraph = null;
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(new CaptureModule(this));
  }
  // End Dagger wiring
}
