package com.genesys.gms.mobile.callback.demo.legacy.data.events;

/**
 * Created by stau on 2/3/2015.
 */
public class GcmRegisterDoneEvent {
    public final String registrationId;

    public GcmRegisterDoneEvent(String registrationId) {
        this.registrationId = registrationId;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "registrationId=" + registrationId +
            "]";
    }
}