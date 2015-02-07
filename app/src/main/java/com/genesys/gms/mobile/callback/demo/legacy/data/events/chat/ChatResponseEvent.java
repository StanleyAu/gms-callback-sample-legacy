package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatResponse;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatResponseEvent {
    public final ChatResponse chatResponse;
    public final ChatRequestType chatRequestType;

    public ChatResponseEvent(ChatResponse chatResponse, ChatRequestType chatRequestType) {
        this.chatResponse = chatResponse;
        this.chatRequestType = chatRequestType;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "chatResponse=" + chatResponse +
            "]";
    }

    public static enum ChatRequestType {
        START,
        REFRESH,
        START_TYPING,
        STOP_TYPING,
        DISCONNECT
    }
}
