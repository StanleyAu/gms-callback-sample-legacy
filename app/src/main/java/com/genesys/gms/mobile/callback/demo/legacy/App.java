package com.genesys.gms.mobile.callback.demo.legacy;

import android.app.Application;
import android.content.res.Configuration;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.CallbackApiManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.ChatApiManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.capture.CaptureManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.OrientationChangeEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.LogbackFacadeTree;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stau on 11/27/2014.
 */
public class App extends Application {
  private ObjectGraph applicationGraph;
  private EventBus bus;
  @Inject
  GcmManager gcmManager;
  @Inject
  CallbackApiManager callbackApiManager;
  @Inject
  ChatApiManager chatApiManager;
  @Inject
  CaptureManager captureManager;

  @Override
  public void onCreate() {
    super.onCreate();
    applicationGraph = ObjectGraph.create(getModules().toArray());
    applicationGraph.inject(this);
    bus = EventBus.getDefault();
    registerManagers();

    if (BuildConfig.DEBUG) {
      Timber.plant(new LogbackFacadeTree(new Timber.DebugTree(), this));
    } else {
      // TODO: Figure out release logging
      Timber.plant(new LogbackFacadeTree(new Timber.HollowTree(), this));
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    switch (newConfig.orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:
        bus.post(new OrientationChangeEvent(Configuration.ORIENTATION_LANDSCAPE));
        break;
      case Configuration.ORIENTATION_PORTRAIT:
        bus.post(new OrientationChangeEvent(Configuration.ORIENTATION_PORTRAIT));
      default:
    }
  }

  public void registerManagers() {
    bus.register(gcmManager);
    bus.register(callbackApiManager);
    bus.register(chatApiManager);
    bus.register(captureManager);
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(new AppModule(this));
  }

  public ObjectGraph getApplicationGraph() {
    return applicationGraph;
  }
}