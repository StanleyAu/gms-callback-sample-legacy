package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatSendEvent {
    public final String serviceId;
    public final String message;
    public final boolean verbose;

    public ChatSendEvent(String serviceId,
                         String message,
                         boolean verbose) {
        this.serviceId = serviceId;
        this.message = message;
        this.verbose = verbose;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceId=" + serviceId +
            ",message=" + message +
            ",verbose=" + verbose +
            "]";
    }
}