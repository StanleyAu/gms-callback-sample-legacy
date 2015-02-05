package com.genesys.gms.mobile.callback.demo.legacy.data.events;

import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackStartEvent {
    public final String serviceName;
    public final String _customer_number;
    public final String _desired_time;
    public final String _callback_state;
    public final String _urs_virtual_queue;
    public final String _request_queue_time_stat;
    public final Map<String, String> properties;

    public CallbackStartEvent(String serviceName,
                            String _customer_number,
                            String _desired_time,
                            String _callback_state,
                            String _urs_virtual_queue,
                            String _request_queue_time_stat,
                            Map<String, String> properties) {
        this.serviceName = serviceName;
        this._customer_number = _customer_number;
        this._desired_time = _desired_time;
        this._callback_state = _callback_state;
        this._urs_virtual_queue = _urs_virtual_queue;
        this._request_queue_time_stat = _request_queue_time_stat;
        this.properties = properties;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceName=" + serviceName +
            ",_customer_number=" + _customer_number +
            ",_desired_time=" + _desired_time +
            ",_callback_state=" + _callback_state +
            ",_urs_virtual_queue=" + _urs_virtual_queue +
            ",_request_queue_time_stat=" + _request_queue_time_stat +
            "properties=" +
            "]";
    }
}
