package com.genesys.gms.mobile.callback.demo.legacy;

import android.app.Application;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.CallbackServiceManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.ChatServiceManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import dagger.ObjectGraph;
import de.greenrobot.event.EventBus;

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

    @Override public void onCreate() {
        super.onCreate();
        applicationGraph = ObjectGraph.create(getModules().toArray());
        applicationGraph.inject(this);
        bus = EventBus.getDefault();
        registerManagers();
        Globals.setupLogging(this);

        if (BuildConfig.DEBUG) {
            //Timber.plant(new Timber.DebugTree());
        } else {
            // TODO: Figure out release logging
            //Timber.plant(new Timber.HollowTree());
        }
    }

    public void registerManagers() {
        bus.register(gcmManager);
        bus.register(callbackServiceManager);
        bus.register(chatServiceManager);
    }

    public void unregisterManagers() {
        bus.unregister(gcmManager);
        bus.unregister(callbackServiceManager);
        bus.unregister(chatServiceManager);
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AppModule(this));
    }

    ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }
}