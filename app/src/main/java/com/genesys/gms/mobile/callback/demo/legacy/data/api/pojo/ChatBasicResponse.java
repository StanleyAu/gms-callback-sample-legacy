package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatBasicResponse {
  @SerializedName("_chatIxnAPI-CREATE-URL")
  private final String createUrl;
  private final String _id;

  public ChatBasicResponse(String createUrl, String _id) {
    this.createUrl = createUrl;
    this._id = _id;
  }

  public String getCreateUrl() {
    return createUrl;
  }

  public String getId() {
    return _id;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "createUrl=" + createUrl +
        ",_id=" + _id +
        "]";
  }
}
