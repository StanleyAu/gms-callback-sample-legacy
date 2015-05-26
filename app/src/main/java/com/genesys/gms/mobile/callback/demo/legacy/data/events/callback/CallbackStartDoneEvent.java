package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackDialog;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackStartDoneEvent {
  public final CallbackDialog callbackDialog;

  public CallbackStartDoneEvent(CallbackDialog callbackDialog) {
    this.callbackDialog = callbackDialog;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "callbackDialog=" + callbackDialog +
        "]";
  }
}
