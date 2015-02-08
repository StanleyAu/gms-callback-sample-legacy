package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by Stan on 2/8/2015.
 */
public class GcmSyncMessage {
    private final String _id;
    private final String _action;
    private final String server_url;
    private final String url_path;

    public GcmSyncMessage(String _id,
                          String _action,
                          String server_url,
                          String url_path) {
        this._id = _id;
        this._action = _action;
        this.server_url = server_url;
        this.url_path = url_path;
    }

    public String getId() {
        return _id;
    }

    public String getAction() {
        return _action;
    }

    public String getServerUrl() {
        return server_url;
    }

    public String getUrlPath() {
        return url_path;
    }

    public String getSyncUrl() {
        return server_url + url_path  + _id + "/" + _action;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
                "[" +
                "_id=" + _id +
                ",_action=" + _action +
                ",server_url=" + server_url +
                ",url_path=" + url_path +
                "]";
    }
}
