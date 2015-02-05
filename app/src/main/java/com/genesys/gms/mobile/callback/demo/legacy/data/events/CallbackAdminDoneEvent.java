package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackAdminRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackAdminDoneEvent {
    public final Map<String, List<CallbackAdminRequest>> callbackServices;

    public CallbackAdminDoneEvent(Map<String, List<CallbackAdminRequest>> callbackServices) {
        this.callbackServices = callbackServices;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "callbackServices=" + callbackServices +
            "]";
    }
}
