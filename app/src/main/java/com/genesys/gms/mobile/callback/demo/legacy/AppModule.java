package com.genesys.gms.mobile.callback.demo.legacy;

import android.app.Application;
import android.content.Context;
import android.media.projection.MediaProjectionManager;
import android.support.v4.app.NotificationManagerCompat;
import android.view.WindowManager;
import com.genesys.gms.mobile.callback.demo.legacy.common.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.data.DataModule;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by stau on 11/27/2014.
 */
@Module(
    injects = {
        App.class
    },
    includes = {
        DataModule.class
    },
    library = true
)
public class AppModule {
  private final App application;

  public AppModule(App application) {
    this.application = application;
  }

  @Provides
  @Singleton
  GoogleCloudMessaging provideGoogleCloudMessaging(@ForApplication Context context) {
    return GoogleCloudMessaging.getInstance(context);
  }

  @Provides
  @Singleton
  WindowManager provideWindowManager(@ForApplication Context context) {
    return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
  }

  @Provides
  @Singleton
  NotificationManagerCompat provideNotificationManagerCompat(@ForApplication Context context) {
    return NotificationManagerCompat.from(context);
  }

  @SuppressWarnings("ResourceType")
  @Provides
  @Singleton
  MediaProjectionManager provideMediaProjectionManager(@ForApplication Context context) {
    return (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
  }

  @Provides
  @Singleton
  @ForApplication
  Context provideApplicationContext() {
    return application;
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return application;
  }
}