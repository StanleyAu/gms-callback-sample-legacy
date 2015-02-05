package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import retrofit.RetrofitError;

/**
 * Created by stau on 2/4/2015.
 */
public class UnknownErrorEvent {
    public final RetrofitError error;

    public UnknownErrorEvent(RetrofitError error) {
        this.error = error;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "error=" + error.toString() +
            "]";
    }
}
