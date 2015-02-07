package com.genesys.gms.mobile.callback.demo.legacy.data.retrofit;

import retrofit.RequestInterceptor;

/**
 * Created by stau on 2/6/2015.
 */
public class GmsRequestInterceptor implements RequestInterceptor {
    private static final String GMS_USER = "gms_user";
    private String gmsUser;

    public String getGmsUser() {
        return gmsUser;
    }

    public void setGmsUser(String newUser) {
        this.gmsUser = newUser;
    }

    @Override
    public void intercept(RequestFacade request) {
        if(gmsUser == null || gmsUser.isEmpty()) {
            return;
        }
        request.addHeader(GMS_USER, gmsUser);
    }
}
