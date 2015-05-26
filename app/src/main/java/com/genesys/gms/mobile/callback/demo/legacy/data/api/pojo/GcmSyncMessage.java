package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by Stan on 2/8/2015.
 */
public class GcmSyncMessage {
  private final String _id;
  private final String _action;

  public GcmSyncMessage(String _id,
                        String _action) {
    this._id = _id;
    this._action = _action;
  }

  public String getId() {
    return _id;
  }

  public String getAction() {
    return _action;
  }

  public String getSyncUri() {
    return _id + "/" + _action;
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() +
        "[" +
        "_id=" + _id +
        ",_action=" + _action +
        "]";
  }
}
