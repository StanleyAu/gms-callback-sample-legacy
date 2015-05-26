package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

/**
 * Created by Stan on 2/8/2015.
 */
public class CallbackCheckQueueEvent {
  public final String sessionId;

  public CallbackCheckQueueEvent(String sessionId) {
    this.sessionId = sessionId;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "sessionId=" + sessionId +
        "]";
  }
}
