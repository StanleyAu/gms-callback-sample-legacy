package com.genesys.gms.mobile.callback.demo.legacy.data.gson;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatPartyType;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by stau on 2/6/2015.
 */
public class TranscriptEntryTypeAdapter extends TypeAdapter<TranscriptEntry> {
    @Override
    public void write(JsonWriter out, TranscriptEntry value) throws IOException {
        if(value == null){
            out.nullValue();
            return;
        }
        out.beginArray();
        out.value(value.getChatEvent().name());
        out.value(value.getNickname());
        out.value(value.getText());
        out.value(value.getPartyId());
        out.value(value.getChatPartyType().name());
        out.endArray();
    }

    @Override
    public TranscriptEntry read(JsonReader in) throws IOException {
        ChatEvent event = null;
        String nickname = null;
        String text = null;
        String partyId = null;
        ChatPartyType partyType = null;

        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        in.beginArray();
        event = ChatEvent.valueOf(in.nextString());
        nickname = in.nextString();
        text = in.nextString();
        partyId = in.nextString();
        partyType = ChatPartyType.valueOf(in.nextString());
        in.endArray();

        return new TranscriptEntry(event, nickname, text, partyId, partyType);
    }
}
