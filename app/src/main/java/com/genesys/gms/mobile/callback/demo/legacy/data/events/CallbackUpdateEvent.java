package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackUpdateEvent {
    public final String serviceName;
    public final String serviceID;
    public final DateTime _new_desired_time;
    public final String _callback_state;
    public final Map<String, String> properties;

    public CallbackUpdateEvent(String serviceName,
                               String serviceID,
                               DateTime _new_desired_time,
                               String _callback_state,
                               Map<String, String> properties) {
        this.serviceName = serviceName;
        this.serviceID = serviceID;
        this._new_desired_time = _new_desired_time;
        this._callback_state = _callback_state;
        this.properties = properties;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceName=" + serviceName +
            ",serviceID=" + serviceID +
            ",_new_desired_time=" + _new_desired_time +
            ",_callback_state=" + _callback_state +
            ",properties=" + properties +
            "]";
    }
}
