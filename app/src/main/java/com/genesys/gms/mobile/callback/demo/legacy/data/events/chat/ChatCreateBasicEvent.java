package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

import java.util.Map;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatCreateBasicEvent {
    public final boolean verbose;
    public final Map<String, String> params;

    public ChatCreateBasicEvent(boolean verbose,
                                Map<String, String> params) {
        this.verbose = verbose;
        this.params = params;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "verbose=" + verbose +
            ",params=" + params +
            "]";
    }
}
