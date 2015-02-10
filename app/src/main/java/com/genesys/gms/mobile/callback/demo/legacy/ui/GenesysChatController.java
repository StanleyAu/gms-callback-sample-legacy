package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.ForActivity;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent;
import com.genesys.gms.mobile.callback.demo.legacy.client.CometClient;
import com.genesys.gms.mobile.callback.demo.legacy.client.CometHandler;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatCometResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatDisconnectEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatResponseEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatResponseEvent.ChatRequestType;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatStartEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatTranscriptEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Stan on 2/8/2015.
 */
public class GenesysChatController implements CometHandler {
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String GMS_USER = "gms_user";
    private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final EventBus bus;
    private final Gson gson;

    private CometClient cometClient;

    // Should store below in a separate Model
    private String sessionId;
    private String serverUrl;
    private String subject;

    @Inject
    public GenesysChatController(@ForActivity Context context,
                                 SharedPreferences sharedPreferences,
                                 HttpClient httpClient,
                                 Gson gson) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.cometClient = new CometClient(httpClient, this);
        this.gson = gson;
        this.bus = EventBus.getDefault();
    }

    // TODO: Functionality first, refactor later.

    // Must be Async
    public void startComet(String serverUrl) {
        this.serverUrl = serverUrl;
        String gmsUser = sharedPreferences.getString(GMS_USER, null);
        cometClient.start(serverUrl, gmsUser);
    }

    // Must be Async
    public void stopComet() {
        cometClient.disconnect();
    }

    public void startChat(String sessionId, String subject) {
        this.sessionId = sessionId;
        this.subject = subject;
        bus.post(new ChatStartEvent(
            sessionId,
            true,
            "comet",
            sharedPreferences.getString("first_name", null),
            sharedPreferences.getString("last_name", null),
            sharedPreferences.getString("chat_email", null),
            subject,
            null, // subscriptionID
            sharedPreferences.getString("chat_display_name", null),
            sharedPreferences.getString(PROPERTY_REG_ID, null),
            "gcm", // pushNotificationType
            null, // pushNotificationLanguage
            false // pushNotificationDebug
        ));
    }

    public void disconnectChat() {
        if(sessionId != null && !sessionId.isEmpty()) {
            bus.post(new ChatDisconnectEvent(sessionId, true));
        }
    }

    @Override
    public void onConnect() {
        // Bayeux client connected
    }

    @Override
    public void onDisconnect() {
        // Bayeux client disconnected
    }

    @Override
    public void onMessage(ClientSessionChannel channel, Message message) {
        ChatCometResponse chatCometResponse;
        try {
            chatCometResponse = gson.fromJson(message.getJSON(), ChatCometResponse.class);
        } catch (JsonSyntaxException e) {
            Log.e("GenesysChatController", "Exception while parsing Comet message: " + e);
            return;
        }
        try {
            ChatResponse chatResponse = chatCometResponse.getData().getMessage();
            int transcriptPosition = chatResponse.getTranscriptPosition();
            if(transcriptPosition <= cometClient.getTranscriptPosition()) {
                log.debug("Comet client is ahead of server!");
                return;
            }
            cometClient.setTranscriptPosition(transcriptPosition);
            List<TranscriptEntry> transcriptEntryList = chatResponse.getTranscriptToShow();
            for(TranscriptEntry entry : transcriptEntryList) {
                bus.post(new ChatTranscriptEvent(entry));
            }
        } catch (NullPointerException e) {
            Log.e("GenesysChatController", "Failed to process transcript entries: " + e);
        }
    }

    public void handleStart(ChatResponse chatResponse) {

    }

    public void handleSend(ChatResponse chatResponse) {

    }

    public void handleRefresh(ChatResponse chatResponse) {

    }

    public void handleStartTyping(ChatResponse chatResponse) {

    }

    public void handleStopTyping(ChatResponse chatResponse) {

    }

    public void handleDisconnect(ChatResponse chatResponse) {

    }
}
