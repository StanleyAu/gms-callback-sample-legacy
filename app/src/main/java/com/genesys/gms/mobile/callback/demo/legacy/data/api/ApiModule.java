package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.content.SharedPreferences;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.ui.Globals;
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
        String host = sharedPreferences.getString(Globals.PROPERTY_HOST, "");
        String strPort = sharedPreferences.getString(Globals.PROPERTY_PORT, "8080");
        String strApiVersion = sharedPreferences.getString(Globals.PROPERTY_API_VERSION, "1");
        if(!host.isEmpty() && !strPort.isEmpty() && !strApiVersion.isEmpty()) {
            int port = Integer.parseInt(strPort);
            int apiVersion = Integer.parseInt(strApiVersion);
            gmsEndpoint.setUrl(host, port, apiVersion);
        }
        return gmsEndpoint;
    }

    @Provides @Singleton
    Client provideClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides @Singleton
    RestAdapter provideRestAdapter(GmsEndpoint endpoint, Client client, Gson gson) {
        return new RestAdapter.Builder()
            .setClient(client)
            .setEndpoint(endpoint)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    @Provides @Singleton
    CallbackService provideNotificationService(RestAdapter restAdapter) {
        return restAdapter.create(CallbackService.class);
    }
}