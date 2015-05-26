package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatRefreshEvent {
  public final String serviceId;
  public final int transcriptPosition;
  public final String message;
  public final boolean verbose;

  public ChatRefreshEvent(String serviceId,
                          int transcriptPosition,
                          String message,
                          boolean verbose) {
    this.serviceId = serviceId;
    this.transcriptPosition = transcriptPosition;
    this.message = message;
    this.verbose = verbose;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "serviceId=" + serviceId +
        ",transcriptPosition=" + transcriptPosition +
        ",message=" + message +
        ",verbose=" + verbose +
        "]";
  }
}