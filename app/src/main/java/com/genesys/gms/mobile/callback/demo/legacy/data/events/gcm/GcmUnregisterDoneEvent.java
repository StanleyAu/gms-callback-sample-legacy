package com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm;

/**
 * Created by stau on 2/5/2015.
 */
public class GcmUnregisterDoneEvent {
    public final String strNewSenderId;

    public GcmUnregisterDoneEvent(String strNewSenderId) {
        this.strNewSenderId = strNewSenderId;
    }

    public boolean isPendingWork() {
        return this.strNewSenderId != null && !this.strNewSenderId.isEmpty();
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "strNewSenderId=" + strNewSenderId +
            "]";
    }
}
