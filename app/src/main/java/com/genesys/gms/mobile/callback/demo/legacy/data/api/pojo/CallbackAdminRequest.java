package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 02/11/2014.
 */
public class CallbackAdminRequest {
  private final String _id;
  private final String desiredTime;
  private final String url;
  private final String _customerNumber;
  private final String _callbackState;

  public CallbackAdminRequest(String _id,
                              String desiredTime,
                              String url,
                              String _customerNumber,
                              String _callbackState) {
    this._id = _id;
    this.desiredTime = desiredTime;
    this.url = url;
    this._customerNumber = _customerNumber;
    this._callbackState = _callbackState;
  }

  public String getId() {
    return _id;
  }

  public String getDesiredTime() {
    return desiredTime;
  }

  public String getUrl() {
    return url;
  }

  public String getCustomerNumber() {
    return _customerNumber;
  }

  public String getCallbackState() {
    return _callbackState;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "_id=" + _id +
        ",desiredTime=" + desiredTime +
        ",url=" + url +
        ",_customerNumber=" + _customerNumber +
        ",_callbackState=" + _callbackState +
        "]";
  }
}