package com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm;

/**
 * Created by stau on 2/3/2015.
 */
public class GcmRegisterEvent {
  public final String senderId;

  public GcmRegisterEvent(String senderId) {
    this.senderId = senderId;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "senderId=" + senderId +
        "]";
  }
}
