package com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm;

import java.io.IOException;

/**
 * Created by Stan on 11/30/2014.
 */
public class GcmErrorEvent {
    public final IOException error;

    public GcmErrorEvent(IOException error) {
        this.error = error;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "error=" + error +
            "]";
    }
}