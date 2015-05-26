package com.genesys.gms.mobile.callback.demo.legacy.data.events.callback;

import org.joda.time.DateTime;

/**
 * Created by stau on 2/3/2015.
 */
public class CallbackAvailabilityEvent {
  public final String serviceName;
  public final DateTime start;
  public final Integer numberOfDays;
  public final DateTime end;
  public final Integer maxTimeSlots;

  public CallbackAvailabilityEvent(String serviceName,
                                   DateTime start,
                                   Integer numberOfDays,
                                   DateTime end,
                                   Integer maxTimeSlots) {
    this.serviceName = serviceName;
    this.start = start;
    this.numberOfDays = numberOfDays;
    this.end = end;
    this.maxTimeSlots = maxTimeSlots;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "serviceName=" + serviceName +
        ",start=" + start +
        ",numberOfDays=" + numberOfDays +
        ",end=" + end +
        ",maxTimeSlots=" + maxTimeSlots +
        "]";
  }
}
