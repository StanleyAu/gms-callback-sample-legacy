package com.genesys.gms.mobile.callback.demo.legacy.data.events;

/**
 * Created by stau on 6/5/2015.
 */
public class OrientationChangeEvent {
  public final int orientation;

  public OrientationChangeEvent(int orientation) {
    this.orientation = orientation;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "orientation=" + orientation +
        "]";
  }
}
