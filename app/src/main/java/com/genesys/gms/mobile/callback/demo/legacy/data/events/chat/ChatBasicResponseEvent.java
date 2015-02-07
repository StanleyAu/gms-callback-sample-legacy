package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatBasicResponse;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatBasicResponseEvent {
    public final ChatBasicResponse chatBasicResponse;

    public ChatBasicResponseEvent(ChatBasicResponse chatBasicResponse) {
        this.chatBasicResponse = chatBasicResponse;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "chatBasicResponse=" + chatBasicResponse +
            "]";
    }
}
