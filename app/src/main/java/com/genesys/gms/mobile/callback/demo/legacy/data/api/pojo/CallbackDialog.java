package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by stau on 2/5/2015.
 * In the absence of a specification, this is a generic catch-all for Callback Dialogs
 */
public class CallbackDialog {
  private final String error;
  private final String _dialogId;
  private final String _id;
  private final Action _action;
  private final String _tel_url;
  private final String _label;
  private final List<DialogGroup> _content;
  private final String _start_chat_url;
  private final String _comet_url;
  private final ChatParameters _chat_parameters;
  private final String _text;
  private final String _okTitle;

  public static enum Action {
    @SerializedName("DialNumber")DIAL,
    @SerializedName("DisplayMenu")MENU,
    @SerializedName("StartChat")CHAT,
    @SerializedName("ConfirmationDialog")CONFIRM
  }

  public CallbackDialog(String error,
                        String _dialogId,
                        String _id,
                        Action _action,
                        String _tel_url,
                        String _label,
                        List<DialogGroup> _content,
                        String _start_chat_url,
                        String _comet_url,
                        ChatParameters _chat_parameters,
                        String _text,
                        String _okTitle) {
    this.error = error;
    this._dialogId = _dialogId;
    this._id = _id;
    this._action = _action;
    this._tel_url = _tel_url;
    this._label = _label;
    this._content = _content;
    this._start_chat_url = _start_chat_url;
    this._comet_url = _comet_url;
    this._chat_parameters = _chat_parameters;
    this._text = _text;
    this._okTitle = _okTitle;
  }

  public String getError() {
    return error;
  }

  public String getDialogId() {
    return _dialogId;
  }

  public String getId() {
    return _id;
  }

  public Action getAction() {
    return _action;
  }

  public String getTelUrl() {
    return _tel_url;
  }

  public String getLabel() {
    return _label;
  }

  public List<DialogGroup> getContent() {
    return _content;
  }

  public String getStartChatUrl() {
    return _start_chat_url;
  }

  public String getCometUrl() {
    return _comet_url;
  }

  public ChatParameters getChatParameters() {
    return _chat_parameters;
  }

  public String getText() {
    return _text;
  }

  public String getOkTitle() {
    return _okTitle;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "error=" + error +
        ",_dialogId=" + _dialogId +
        ",_id=" + _id +
        ",_action=" + _action +
        ",_tel_url=" + _tel_url +
        ",_label=" + _label +
        ",_content=" + _content +
        ",_start_chat_url=" + _start_chat_url +
        ",_comet_url=" + _comet_url +
        ",_chat_parameters=" + _chat_parameters +
        ",_text=" + _text +
        ",_okTitle=" + _okTitle +
        "]";
  }

  public class DialogGroup {
    private final String _group_name;
    private final List<GroupContent> _group_content;

    public DialogGroup(String _group_name,
                       List<GroupContent> _group_content) {
      this._group_name = _group_name;
      this._group_content = _group_content;
    }

    public String getGroupName() {
      return _group_name;
    }

    public List<GroupContent> getGroupContent() {
      return _group_content;
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + hashCode() +
          "[" +
          "_group_name=" + _group_name +
          ",_group_content=" + _group_content +
          "]";
    }
  }

  public class GroupContent {
    private final String _label;
    private final String _user_action_url;

    public GroupContent(String _label,
                        String _user_action_url) {
      this._label = _label;
      this._user_action_url = _user_action_url;
    }

    public String getLabel() {
      return _label;
    }

    public String getUserActionUrl() {
      return _user_action_url;
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + hashCode() +
          "[" +
          "_label=" + _label +
          ",_user_action_url=" + _user_action_url +
          "]";
    }
  }

  public class ChatParameters {
    private final String subject;

    public ChatParameters(String subject) {
      this.subject = subject;
    }

    public String getSubject() {
      return subject;
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + hashCode() +
          "[" +
          "subject=" + subject +
          "]";
    }
  }
}
