package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackException;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackErrorEvent {
  public final CallbackException callbackException;

  public CallbackErrorEvent(CallbackException callbackException) {
    this.callbackException = callbackException;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "callbackException=" + callbackException +
        "]";
  }
}
