package com.genesys.gms.mobile.callback.demo.legacy.data.capture;

import android.content.Context;
import com.genesys.gms.mobile.callback.demo.legacy.AppModule;
import com.genesys.gms.mobile.callback.demo.legacy.common.ForService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by stau on 5/26/2015.
 */
@Module(
    injects = {
        CaptureService.class
    },
    addsTo = AppModule.class,
    library = true
)
public class CaptureModule {
    private final CaptureService captureService;
    public CaptureModule(CaptureService captureService) {
        this.captureService = captureService;
    }
    @Provides
    @Singleton
    @ForService
    Context provideCaptureServiceContext() {
        return captureService;
    }
}
