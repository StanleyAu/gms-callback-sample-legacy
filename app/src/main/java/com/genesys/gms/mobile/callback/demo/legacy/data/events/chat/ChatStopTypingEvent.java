package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatStopTypingEvent {
    public final String serviceId;
    public final boolean verbose;

    public ChatStopTypingEvent(String serviceId,
                               boolean verbose) {
        this.serviceId = serviceId;
        this.verbose = verbose;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceId=" + serviceId +
            ",verbose=" + verbose +
            "]";
    }
}
