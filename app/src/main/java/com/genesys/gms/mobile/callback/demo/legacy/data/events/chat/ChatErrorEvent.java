package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatException;

/**
 * Created by stau on 2/3/2015.
 */
public class ChatErrorEvent {
  public final ChatException chatException;

  public ChatErrorEvent(ChatException chatException) {
    this.chatException = chatException;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "chatException=" + chatException +
        "]";
  }
}
