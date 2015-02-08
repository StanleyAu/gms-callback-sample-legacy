package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackQueuePosition;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackCheckQueueDoneEvent {
    public final boolean success;
    public final CallbackQueuePosition callbackQueuePosition;

    public CallbackCheckQueueDoneEvent(boolean success, CallbackQueuePosition callbackQueuePosition) {
        this.success = success;
        this.callbackQueuePosition = callbackQueuePosition;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "success=" + success +
                ",callbackQueuePosition=" + callbackQueuePosition +
                "]";
    }
}
