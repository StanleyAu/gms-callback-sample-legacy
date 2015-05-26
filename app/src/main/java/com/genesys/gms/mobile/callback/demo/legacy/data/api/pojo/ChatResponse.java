package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import java.util.List;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatResponse {
  private final ChatState chatIxnState;
  private final String chatSessionId;
  private final int transcriptPosition;
  private final String chatServiceMessage;
  private final List<TranscriptEntry> transcriptToShow;
  private final String startedAt; // Different DateTime format YYYY-MM-DDTHH:MM:SSZ

  // VERBOSE fields START
  private final String userId;
  private final String secureKey;
  private final String checkChatServiceLoadBalancerPath;
  private final String chatServerLoadBalancerAlias;
  private final String chatServerHost;
  private final int chatWebApiPort;
  private final boolean isTLSrequired;
  private final String clientTimeZoneOffset;

  private final String _chatIxnAPI_SEND_URL;
  private final String _chatIxnAPI_REFRESH_URL;
  private final String _chatIxnAPI_START_TYPING_URL;
  private final String _chatIxnAPI_STOP_TYPING_URL;
  private final String _chatIxnAPI_DISCONNECT_URL;
  private final String _chatIxnAPI_REFRESH_FROM_START_URL;
  // VERBOSE fields END

  public ChatResponse(ChatState chatIxnState,
                      String chatSessionId,
                      int transcriptPosition,
                      String chatServiceMessage,
                      List<TranscriptEntry> transcriptToShow,
                      String startedAt,
                      String userId,
                      String secureKey,
                      String checkChatServiceLoadBalancerPath,
                      String chatServerLoadBalancerAlias,
                      String chatServerHost,
                      int chatWebApiPort,
                      boolean isTLSrequired,
                      String clientTimeZoneOffset,
                      String _chatIxnAPI_SEND_URL,
                      String _chatIxnAPI_REFRESH_URL,
                      String _chatIxnAPI_START_TYPING_URL,
                      String _chatIxnAPI_STOP_TYPING_URL,
                      String _chatIxnAPI_DISCONNECT_URL,
                      String _chatIxnAPI_REFRESH_FROM_START_URL) {
    this.chatIxnState = chatIxnState;
    this.chatSessionId = chatSessionId;
    this.transcriptPosition = transcriptPosition;
    this.chatServiceMessage = chatServiceMessage;

    this.transcriptToShow = transcriptToShow;
    this.startedAt = startedAt;

    this.userId = userId;
    this.secureKey = secureKey;
    this.checkChatServiceLoadBalancerPath = checkChatServiceLoadBalancerPath;
    this.chatServerLoadBalancerAlias = chatServerLoadBalancerAlias;
    this.chatServerHost = chatServerHost;
    this.chatWebApiPort = chatWebApiPort;
    this.isTLSrequired = isTLSrequired;
    this.clientTimeZoneOffset = clientTimeZoneOffset;
    this._chatIxnAPI_SEND_URL = _chatIxnAPI_SEND_URL;
    this._chatIxnAPI_REFRESH_URL = _chatIxnAPI_REFRESH_URL;
    this._chatIxnAPI_START_TYPING_URL = _chatIxnAPI_START_TYPING_URL;
    this._chatIxnAPI_STOP_TYPING_URL = _chatIxnAPI_STOP_TYPING_URL;
    this._chatIxnAPI_DISCONNECT_URL = _chatIxnAPI_DISCONNECT_URL;
    this._chatIxnAPI_REFRESH_FROM_START_URL = _chatIxnAPI_REFRESH_FROM_START_URL;
  }

  public ChatState getChatIxnState() {
    return chatIxnState;
  }

  public String getChatSessionId() {
    return chatSessionId;
  }

  public int getTranscriptPosition() {
    return transcriptPosition;
  }

  public String getChatServiceMessage() {
    return chatServiceMessage;
  }

  public List<TranscriptEntry> getTranscriptToShow() {
    return transcriptToShow;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public String getUserId() {
    return userId;
  }

  public String getSecureKey() {
    return secureKey;
  }

  public String getCheckChatServiceLoadBalancerPath() {
    return checkChatServiceLoadBalancerPath;
  }

  public String getChatServerLoadBalancerAlias() {
    return chatServerLoadBalancerAlias;
  }

  public String getChatServerHost() {
    return chatServerHost;
  }

  public int getChatWebApiPort() {
    return chatWebApiPort;
  }

  public boolean isTLSRequired() {
    return isTLSrequired;
  }

  public String getClientTimeZoneOffset() {
    return clientTimeZoneOffset;
  }

  public String getChatSendUrl() {
    return _chatIxnAPI_SEND_URL;
  }

  public String getChatRefreshUrl() {
    return _chatIxnAPI_REFRESH_URL;
  }

  public String getChatStartTypingUrl() {
    return _chatIxnAPI_START_TYPING_URL;
  }

  public String getChatStopTypingUrl() {
    return _chatIxnAPI_STOP_TYPING_URL;
  }

  public String getChatDisconnectUrl() {
    return _chatIxnAPI_DISCONNECT_URL;
  }

  public String getChatRefreshFromStartUrl() {
    return _chatIxnAPI_REFRESH_FROM_START_URL;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "chatIxnState=" + chatIxnState +
        ",chatSessionId=" + chatSessionId +
        ",transcriptPosition=" + transcriptPosition +
        ",chatServiceMessage=" + chatServiceMessage +
        ",transcriptToShow=" + transcriptToShow +
        ",startedAt=" + startedAt +
        ",userId=" + userId +
        ",secureKey=" + secureKey +
        ",checkChatServiceLoadBalancerPath=" + checkChatServiceLoadBalancerPath +
        ",chatServerLoadBalancerAlias=" + chatServerLoadBalancerAlias +
        ",chatServerHost=" + chatServerHost +
        ",chatWebApiPort=" + chatWebApiPort +
        ",isTLSrequired=" + isTLSrequired +
        ",clientTimeZoneOffset=" + clientTimeZoneOffset +
        ",_chatIxnAPI_SEND_URL=" + _chatIxnAPI_SEND_URL +
        ",_chatIxnAPI_REFRESH_URL=" + _chatIxnAPI_REFRESH_URL +
        ",_chatIxnAPI_START_TYPING_URL=" + _chatIxnAPI_START_TYPING_URL +
        ",_chatIxnAPI_STOP_TYPING_URL=" + _chatIxnAPI_STOP_TYPING_URL +
        ",_chatIxnAPI_DISCONNECT_URL=" + _chatIxnAPI_DISCONNECT_URL +
        ",_chatIxnAPI_REFRESH_FROM_START_URL=" + _chatIxnAPI_REFRESH_FROM_START_URL +
        "]";
  }
}
