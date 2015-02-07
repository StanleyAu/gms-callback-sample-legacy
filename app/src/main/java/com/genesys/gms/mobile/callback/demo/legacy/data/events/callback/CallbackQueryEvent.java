package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackQueryEvent {
    public final String serviceName;
    public final Operand operand;
    public final Map<String, String> properties;

    public enum Operand {
        AND,
        OR
    }

    public CallbackQueryEvent(String serviceName,
                              Operand operand,
                              Map<String, String> properties) {
        this.serviceName = serviceName;
        this.operand = operand;
        this.properties = properties;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceName=" + serviceName +
            ",operand=" + operand +
            ",properties=" + properties +
            "]";
    }
}
