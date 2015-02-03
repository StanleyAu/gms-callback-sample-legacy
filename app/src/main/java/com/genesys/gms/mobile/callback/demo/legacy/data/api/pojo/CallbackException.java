package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 02/11/2014.
 */
public class CallbackException {
    private final String message;
    private final String exception;
    private final String error;

    public CallbackException(String message, String exception, String error) {
        this.message = message;
        this.exception = exception;
        this.error = error;
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

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "message=" + message +
            ",exception=" + exception +
            ",error=" + error +
            "]";
    }
}