package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 02/11/2014.
 */
public class CallbackConfirmationDialog {
    private final String _dialogId;
    private final String _action;
    private final String _text;
    private final String _okTitle;
    private final String _id;

    public CallbackConfirmationDialog(String _dialogId,
                                      String _action,
                                      String _text,
                                      String _okTitle,
                                      String _id) {
        this._dialogId = _dialogId;
        this._action = _action;
        this._text = _text;
        this._okTitle = _okTitle;
        this._id = _id;
    }

    public String getDialogId() {
        return _dialogId;
    }

    public String getAction() {
        return _action;
    }

    public String getText() {
        return _text;
    }

    public String getOkTitle() {
        return _okTitle;
    }

    public String getId() {
        return _id;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "_dialogId=" + _dialogId +
            ",_action=" + _action +
            ",_text=" + _text +
            ",_okTitle=" + _okTitle +
            ",_id=" + _id +
            "]";
    }
}