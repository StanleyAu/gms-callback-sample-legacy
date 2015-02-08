package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackDialog;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackDialogDoneEvent {
    public final boolean success;
    public final CallbackDialog callbackDialog;

    public CallbackDialogDoneEvent(boolean success, CallbackDialog callbackDialog) {
        this.success = success;
        this.callbackDialog = callbackDialog;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "success=" + success +
                ",callbackDialog=" + callbackDialog +
                "]";
    }
}
