package com.genesys.gms.mobile.callback.demo.legacy.data.async;

import android.os.AsyncTask;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmRegisterEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmUnregisterDoneEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.greenrobot.event.EventBus;

import java.io.IOException;

/**
 * Created by Stan on 11/30/2014.
 *
 * GCM unregister() is an expensive call that should normally never be
 * used in a GCM client. However, we want to be able to hot-swap
 * Sender IDs during runtime.
 */
public class GcmUnregisterAsync extends AsyncTask<Void, Void, Boolean> {
    private final GoogleCloudMessaging googleCloudMessaging;
    private final EventBus bus;
    private IOException savedException;
    private String newSenderId;

    public GcmUnregisterAsync(GoogleCloudMessaging googleCloudMessaging) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.bus = EventBus.getDefault();
        this.newSenderId = null;
    }

    public GcmUnregisterAsync(GoogleCloudMessaging googleCloudMessaging, String newSenderId) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.bus = EventBus.getDefault();
        this.newSenderId = newSenderId;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            googleCloudMessaging.unregister();
            return true;
        } catch (IOException e) {
            savedException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result == true){
            bus.post(new GcmUnregisterDoneEvent(newSenderId));
        } else {
            bus.post(new GcmErrorEvent(savedException));
        }
    }
}