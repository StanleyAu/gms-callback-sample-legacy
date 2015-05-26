package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by stau on 2/12/2015.
 */
public class GcmChatMessage {
  private final String message;
  private final String serviceId;
  private final List<TranscriptBrief> lastTranscript;

  public GcmChatMessage(String message,
                        String serviceId,
                        List<TranscriptBrief> lastTranscript) {
    this.message = message;
    this.serviceId = serviceId;
    this.lastTranscript = lastTranscript;
  }

  public String getMessage() {
    return message;
  }

  public String getServiceId() {
    return serviceId;
  }

  public List<TranscriptBrief> getLastTranscript() {
    return lastTranscript;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "message=" + message +
        ",serviceId=" + serviceId +
        ",lastTranscript=" + lastTranscript +
        "]";
  }

  public static class TranscriptBrief {
    @SerializedName("Message.Text")
    private final String messageText;

    public TranscriptBrief(String messageText) {
      this.messageText = messageText;
    }

    public String getMessageText() {
      return messageText;
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + hashCode() +
          "[" +
          "messageText=" + messageText +
          "]";
    }
  }
}
