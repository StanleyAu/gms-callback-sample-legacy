package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;

/**
 * Created by stau on 2/9/2015.
 */
public class ChatTranscriptEvent {
  public final TranscriptEntry transcriptEntry;

  public ChatTranscriptEvent(TranscriptEntry transcriptEntry) {
    this.transcriptEntry = transcriptEntry;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "transcriptEntry=" + transcriptEntry +
        "]";
  }
}
