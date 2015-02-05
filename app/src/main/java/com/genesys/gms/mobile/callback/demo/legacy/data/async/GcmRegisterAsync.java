package com.genesys.gms.mobile.callback.demo.legacy.data.async;

import android.os.AsyncTask;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmRegisterDoneEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.greenrobot.event.EventBus;

import java.io.IOException;

/**
 * Created by Stan on 11/30/2014.
 */
public class GcmRegisterAsync extends AsyncTask<Void, Void, String> {
    private final GoogleCloudMessaging googleCloudMessaging;
    private final String senderId;
    private final EventBus bus;
    private IOException savedException;

    public GcmRegisterAsync(GoogleCloudMessaging googleCloudMessaging, String senderId) {
        super();
        this.googleCloudMessaging = googleCloudMessaging;
        this.senderId = senderId;
        this.bus = EventBus.getDefault();
    }
    @Override
    protected String doInBackground(Void... params) {
        try {
            return googleCloudMessaging.register(senderId);
        } catch (IOException e) {
            savedException = e;
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        if(result!=null && !result.isEmpty()) {
            bus.post(new GcmRegisterDoneEvent(result, senderId));
        } else if(savedException!=null) {
            bus.post(new GcmErrorEvent(savedException));
        } else {
            // TODO: Log this unknown error
        }
    }
}