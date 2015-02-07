package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 2/6/2015.
 */
public class TranscriptEntry {
    private final ChatEvent chatEvent;
    private final String nickname;
    private final String text;
    private final int partyId;
    private final ChatPartyType chatPartyType;

    public TranscriptEntry(ChatEvent chatEvent,
                           String nickname,
                           String text,
                           int partyId,
                           ChatPartyType chatPartyType) {
        this.chatEvent = chatEvent;
        this.nickname = nickname;
        this.text = text;
        this.partyId = partyId;
        this.chatPartyType = chatPartyType;
    }

    public ChatEvent getChatEvent() {
        return chatEvent;
    }

    public String getNickname() {
        return nickname;
    }

    public String getText() {
        return text;
    }

    public int getPartyId() {
        return partyId;
    }

    public ChatPartyType getChatPartyType() {
        return chatPartyType;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "chatEvent=" + chatEvent +
            ",nickname=" + nickname +
            ",text=" + text +
            ",partyId=" + partyId +
            "]";
    }
}
