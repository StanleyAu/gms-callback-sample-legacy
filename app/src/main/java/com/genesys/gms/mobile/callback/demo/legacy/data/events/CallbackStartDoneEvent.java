package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackConfirmationDialog;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackStartDoneEvent {
    public final CallbackConfirmationDialog callbackConfirmationDialog;

    public CallbackStartDoneEvent(CallbackConfirmationDialog callbackConfirmationDialog) {
        this.callbackConfirmationDialog = callbackConfirmationDialog;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "callbackConfirmationDialog=" + callbackConfirmationDialog +
            "]";
    }
}
