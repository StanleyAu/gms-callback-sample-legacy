package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackRescheduleException;

/**
 * Created by stau on 2/4/2015.
 */
public class CallbackRescheduleErrorEvent {
    public final CallbackRescheduleException callbackRescheduleException;

    public CallbackRescheduleErrorEvent(CallbackRescheduleException callbackRescheduleException) {
        this.callbackRescheduleException = callbackRescheduleException;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "callbackRescheduleException=" + callbackRescheduleException +
            "]";
    }
}