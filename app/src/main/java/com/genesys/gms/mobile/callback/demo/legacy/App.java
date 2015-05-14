package com.genesys.gms.mobile.callback.demo.legacy;

import android.app.Application;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.CallbackServiceManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.capture.CaptureManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.ChatServiceManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
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
    @Inject GcmManager gcmManager;
    @Inject CallbackServiceManager callbackServiceManager;
    @Inject ChatServiceManager chatServiceManager;
    @Inject CaptureManager captureManager;

    @Override public void onCreate() {
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

    public void registerManagers() {
        bus.register(gcmManager);
        bus.register(callbackServiceManager);
        bus.register(chatServiceManager);
        bus.register(captureManager);
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AppModule(this));
    }

    ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }
}