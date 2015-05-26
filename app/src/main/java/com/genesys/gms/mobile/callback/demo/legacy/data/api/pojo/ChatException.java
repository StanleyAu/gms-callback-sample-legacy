package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 02/11/2014.
 */
public class ChatException {
  private final String message;
  private final String exception;


  public ChatException(String message,
                       String exception) {
    this.message = message;
    this.exception = exception;
  }

  public String getMessage() {
    return message;
  }

  public String getException() {
    return exception;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "message=" + message +
        ",exception=" + exception +
        "]";
  }
}