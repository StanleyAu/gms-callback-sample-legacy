package com.genesys.gms.mobile.callback.demo.legacy.data.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.genesys.gms.mobile.callback.demo.legacy.AppModule;
import com.genesys.gms.mobile.callback.demo.legacy.common.ForService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by stau on 5/26/2015.
 */
@Module(
    injects ={
        GcmIntentService.class
    },
    addsTo = AppModule.class,
    library = true
)
public class GcmModule {
    private final GcmIntentService gcmIntentService;
    public GcmModule(GcmIntentService gcmIntentService) {
        this.gcmIntentService = gcmIntentService;
    }
    @Provides
    @Singleton
    @ForService
    Context provideGcmIntentServiceContext() {
        return gcmIntentService;
    }
}
