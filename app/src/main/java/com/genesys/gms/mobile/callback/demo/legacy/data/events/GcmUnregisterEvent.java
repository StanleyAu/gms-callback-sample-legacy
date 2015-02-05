package com.genesys.gms.mobile.callback.demo.legacy.data.events;

/**
 * Created by stau on 2/5/2015.
 */
public class GcmUnregisterEvent {
    public final String strNewSenderId;

    public GcmUnregisterEvent(String strNewSenderId) {
        this.strNewSenderId = strNewSenderId;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "strNewSenderId=" + strNewSenderId +
            "]";
    }
}
