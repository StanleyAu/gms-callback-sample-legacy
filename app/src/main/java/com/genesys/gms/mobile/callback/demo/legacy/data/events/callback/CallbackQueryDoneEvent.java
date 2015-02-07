package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackRequest;

import java.util.List;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackQueryDoneEvent {
    public final List<CallbackRequest> callbackRequests;

    public CallbackQueryDoneEvent(List<CallbackRequest> callbackRequests) {
        this.callbackRequests = callbackRequests;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "callbackRequests=" + callbackRequests +
            "]";
    }
}
