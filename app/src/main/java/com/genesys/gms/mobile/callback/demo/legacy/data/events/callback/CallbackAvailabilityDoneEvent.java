package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackAvailabilityDoneEvent {
    public final Map<DateTime, Integer> availability;

    public CallbackAvailabilityDoneEvent(Map<DateTime, Integer> availability) {
        this.availability = availability;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "availability=" + availability +
            "]";
    }
}
