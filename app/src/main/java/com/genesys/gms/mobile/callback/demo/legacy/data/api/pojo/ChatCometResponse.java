package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 2/9/2015.
 */
public class ChatCometResponse {
  private final ChatCometData data;
  private final String channel;

  public ChatCometResponse(ChatCometData data, String channel) {
    this.data = data;
    this.channel = channel;
  }

  public ChatCometData getData() {
    return data;
  }

  public String getChannel() {
    return channel;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "data=" + data +
        ",channel=" + channel +
        "]";
  }

  public class ChatCometData {
    private final String id;
    private final ChatResponse message;
    private final String tag;

    public ChatCometData(String id, ChatResponse message, String tag) {
      this.id = id;
      this.message = message;
      this.tag = tag;
    }

    public String getId() {
      return id;
    }

    public ChatResponse getMessage() {
      return message;
    }

    public String getTag() {
      return tag;
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + hashCode() +
          "[" +
          "id=" + id +
          ",message=" + message +
          ",tag=" + tag +
          "]";
    }
  }
}
