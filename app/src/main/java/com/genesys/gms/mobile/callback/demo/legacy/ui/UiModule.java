package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.Context;
import com.genesys.gms.mobile.callback.demo.legacy.AppModule;
import com.genesys.gms.mobile.callback.demo.legacy.common.BaseActivity;
import com.genesys.gms.mobile.callback.demo.legacy.common.ForActivity;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by stau on 11/27/2014.
 */
@Module(
    injects = {
        GenesysSampleActivity.class,
        GenesysChatActivity.class,
        LogActivity.class,
        LogFragment.class,
        PreferenceWithSummaryFragment.class
    },
    addsTo = AppModule.class,
    library = true
)
public class UiModule {
  private final BaseActivity activity;

  public UiModule(BaseActivity activity) {
    this.activity = activity;
  }

  @Provides
  @Singleton
  @ForActivity
  Context provideActivityContext() {
    return activity;
  }
}