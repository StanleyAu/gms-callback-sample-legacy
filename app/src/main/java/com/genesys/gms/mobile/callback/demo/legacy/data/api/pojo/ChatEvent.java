package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stau on 2/6/2015.
 */
public enum ChatEvent {
  @SerializedName("Message.Text")MESSAGE,
  @SerializedName("Notice.Joined")PARTY_JOINED,
  @SerializedName("Notice.Left")PARTY_LEFT,
  @SerializedName("Notice.TypingStarted")TYPING_STARTED,
  @SerializedName("Notice.TypingStopped")TYPING_STOPPED,
  @SerializedName("Notice.PushUrl")PUSH_URL,
  @SerializedName("Notice.Custom")CUSTOM
}
