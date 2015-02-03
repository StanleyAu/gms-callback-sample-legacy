package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import android.app.IntentService;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by stau on 02/11/2014.
 */
public class CallbackRescheduleException {
    private final String message;
    private final String exception;
    private final String error;
    private final Map<DateTime,Integer> availability;

    public CallbackRescheduleException(String message,
                                       String exception,
                                       String error,
                                       Map<DateTime,Integer> availability) {
        this.message = message;
        this.exception = exception;
        this.error = error;
        this.availability = availability;
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    public String getError() {
        return error;
    }

    public Map<DateTime, Integer> getAvailability() {
        return availability;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "message=" + message +
            ",exception=" + exception +
            ",error=" + error +
            ",availability=" + availability +
            "]";
    }
}