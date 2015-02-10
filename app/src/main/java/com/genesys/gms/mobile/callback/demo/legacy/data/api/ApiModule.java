package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.content.SharedPreferences;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.BuildConfig;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsRequestInterceptor;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import javax.inject.Singleton;

/**
 * Created by stau on 11/27/2014.
 */
@Module(
    complete = false,
    library = true
)
public class ApiModule {
    @Provides @Singleton
    GmsEndpoint provideEndpoint(SharedPreferences sharedPreferences) {
        GmsEndpoint gmsEndpoint = new GmsEndpoint();
        String host = sharedPreferences.getString(Globals.PROPERTY_HOST, "localhost");
        String strPort = sharedPreferences.getString(Globals.PROPERTY_PORT, "8080");
        String strApiVersion = sharedPreferences.getString(Globals.PROPERTY_API_VERSION, "1");
        if(!host.isEmpty() && !strPort.isEmpty() && !strApiVersion.isEmpty()) {
            Integer port = null;
            Integer apiVersion = null;
            try {
                port = Integer.valueOf(strPort);
                apiVersion = Integer.valueOf(strApiVersion);
            } catch (NumberFormatException e) {
                ;
            }
            gmsEndpoint.setUrl(host, port, apiVersion);
        }
        return gmsEndpoint;
    }

    @Provides @Singleton
    GmsRequestInterceptor provideRequestInterceptor(SharedPreferences sharedPreferences) {
        Log.d("GmsRequestInterceptor", "Constructing new GmsRequestInterceptor");
        GmsRequestInterceptor gmsRequestInterceptor = new GmsRequestInterceptor();
        String gmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
        gmsRequestInterceptor.setGmsUser(gmsUser);
        return gmsRequestInterceptor;
    }

    @Provides @Singleton
    Client provideClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides @Singleton
    RestAdapter provideRestAdapter(GmsEndpoint endpoint, GmsRequestInterceptor requestInterceptor, Client client, Gson gson) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
            .setClient(client)
            .setEndpoint(endpoint)
            .setRequestInterceptor(requestInterceptor)
            .setConverter(new GsonConverter(gson));
        if(BuildConfig.DEBUG) {
            builder.setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.d("Retrofit", msg);
                    }
                });
        }
        return builder.build();
    }

    @Provides @Singleton
    CallbackService provideCallbackService(RestAdapter restAdapter) {
        return restAdapter.create(CallbackService.class);
    }

    @Provides @Singleton
    ChatService provideChatService(RestAdapter restAdapter) {
        return restAdapter.create(ChatService.class);
    }
}