package com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo;

/**
 * Created by stau on 02/11/2014.
 */
public class CallbackRequest {
    private final String _id;
    private final String desiredTime;
    private final String url;

    public CallbackRequest(String _id, String desiredTime, String url) {
        this._id = _id;
        this.desiredTime = desiredTime;
        this.url = url;
    }

    public String getId() {
        return _id;
    }

    public String getDesiredTime() {
        return desiredTime;
    }

    public String getUrl() {
        return url;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "_id=" + _id +
            ",desiredTime=" + desiredTime +
            ",url=" + url +
            "]";
    }
}