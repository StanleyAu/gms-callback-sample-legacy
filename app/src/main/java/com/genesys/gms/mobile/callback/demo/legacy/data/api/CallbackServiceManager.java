package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.events.*;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by stau on 2/3/2015.
 */
@Singleton
public class CallbackServiceManager {
    private final CallbackService callbackService;
    private final EventBus bus;

    @DebugLog @Inject
    public CallbackServiceManager(CallbackService callbackService) {
        this.callbackService = callbackService;
        this.bus = EventBus.getDefault();
    }

    public void onEvent(CallbackStartEvent event) {
        ;
    }

    public void onEvent(CallbackCancelEvent event) {
        ;
    }

    public void onEvent(CallbackUpdateEvent event) {
        ;
    }

    public void onEvent(CallbackQueryEvent event) {
        ;
    }

    public void onEvent(CallbackAvailabilityEvent event) {
        ;
    }

    public void onEvent(CallbackAdminEvent event) {
        ;
    }
}
