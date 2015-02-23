package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackDialogEvent {
    public final String url;
    public final boolean isFragment;

    public CallbackDialogEvent(String url, boolean isFragment) {
        this.url = url;
        this.isFragment = isFragment;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "url=" + url +
                ",isFragment=" + isFragment +
                "]";
    }
}
