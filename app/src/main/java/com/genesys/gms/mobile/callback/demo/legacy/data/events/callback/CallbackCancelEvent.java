package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackCancelEvent {
  public final String serviceName;
  public final String serviceID;

  public CallbackCancelEvent(String serviceName,
                             String serviceID) {
    this.serviceName = serviceName;
    this.serviceID = serviceID;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "serviceName=" + serviceName +
        ",serviceID=" + serviceID +
        "]";
  }
}
