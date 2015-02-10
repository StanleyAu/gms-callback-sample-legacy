package com.genesys.gms.mobile.callback.demo.legacy.data.retrofit;

import android.util.Log;
import retrofit.RequestInterceptor;

import javax.inject.Singleton;

/**
 * Created by stau on 2/6/2015.
 */
@Singleton
public class GmsRequestInterceptor implements RequestInterceptor {
    private static final String GMS_USER = "gms_user";
    private String gmsUser;

    public GmsRequestInterceptor() {
        super();
        Log.d("GmsRequestInterceptor", "Constructing new instance.");
    }

    public String getGmsUser() {
        return gmsUser;
    }

    public void setGmsUser(String newUser) {
        Log.d("GmsRequestInterceptor", "Setting gms_user: " + newUser);
        this.gmsUser = newUser;
    }

    @Override
    public void intercept(RequestFacade request) {
        Log.d("GmsRequestInterceptor", "Inserting gms_user: " + gmsUser + " header.");
        if(gmsUser == null || gmsUser.isEmpty()) {
            return;
        }
        request.addHeader(GMS_USER, gmsUser);
    }
}
