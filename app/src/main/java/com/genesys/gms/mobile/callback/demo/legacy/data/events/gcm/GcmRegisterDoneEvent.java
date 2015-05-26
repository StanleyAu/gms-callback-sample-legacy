package com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm;

/**
 * Created by stau on 2/3/2015.
 */
public class GcmRegisterDoneEvent {
  public final String registrationId;
  public final String senderId;

  public GcmRegisterDoneEvent(String registrationId, String senderId) {
    this.registrationId = registrationId;
    this.senderId = senderId;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "registrationId=" + registrationId +
        ",senderId=" + senderId +
        "]";
  }
}