package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackDialogEvent {
    public final String url;

    public CallbackDialogEvent(String url) {
        this.url = url;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "url=" + url +
                "]";
    }
}
