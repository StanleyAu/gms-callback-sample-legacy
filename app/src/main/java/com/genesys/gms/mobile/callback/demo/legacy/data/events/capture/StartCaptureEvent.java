package com.genesys.gms.mobile.callback.demo.legacy.data.events.capture;

import android.content.Intent;

/**
 * Created by stau on 5/12/2015.
 */
public class StartCaptureEvent {
    public final int resultCode;
    public final Intent data;

    public StartCaptureEvent(int resultCode, Intent data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "resultCode=" + resultCode +
                "data=" + data +
                "]";
    }
}
