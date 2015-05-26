package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import org.joda.time.DateTime;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackAdminEvent {
  public final DateTime end_time;
  public final String target;
  public final Integer max;

  public CallbackAdminEvent(DateTime end_time,
                            String target,
                            Integer max) {
    this.end_time = end_time;
    this.target = target;
    this.max = max;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "end_time=" + end_time +
        ",target=" + target +
        ",max=" + max +
        "]";
  }
}
