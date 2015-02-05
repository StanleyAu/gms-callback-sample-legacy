package com.genesys.gms.mobile.callback.demo.legacy.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.ApiModule;
import com.genesys.gms.mobile.callback.demo.legacy.data.gson.DateTimeTypeAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by stau on 11/27/2014.
 */
@Module(
    includes = ApiModule.class,
    complete = false,
    library = true
)
public class DataModule {
    static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    static final String UTC_DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";

    @Provides @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }

    @Provides @Singleton
    DateTimeFormatter provideDateTimeFormatter() {
        return DateTimeFormat.forPattern(UTC_DATE_FORMAT).withZoneUTC();
    }

    @Provides @Singleton
    Gson provideGson() {
        return new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat(UTC_DATE_FORMAT)
            .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
            .setPrettyPrinting()
            .create();
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(@ForApplication Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();

        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(app.getCacheDir(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            client.setCache(cache);
        } catch (IOException e) {
            //Timber.e(e, "Unable to install disk cache.");
        }

        return client;
    }
}