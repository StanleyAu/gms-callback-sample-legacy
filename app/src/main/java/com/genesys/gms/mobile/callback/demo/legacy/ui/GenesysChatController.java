package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.ForActivity;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent;
import com.genesys.gms.mobile.callback.demo.legacy.client.CometClient;
import com.genesys.gms.mobile.callback.demo.legacy.client.CometHandler;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatCometResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatState;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatResponseEvent.ChatRequestType;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
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
    // TODO: Does this controller retain state following orientation change?
    private String sessionId;
    private String serverUrl;
    private String subject;

    @Override public String toString() {
        // For debugging
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "cometClient=" + cometClient +
            ",sessionId=" + sessionId +
            ",serverUrl=" + serverUrl +
            ",subject=" + subject +
            "]";
    }

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

    public void persistState(Bundle outState) {
        // Nothing to do
    }

    public void restoreState(Bundle inState) {
        // From GenesysChatActivity
        sessionId = inState.getString("chatId");
        serverUrl = inState.getString("cometUrl");
        subject = inState.getString("subject");
    }

    // TODO: Functionality first, refactor later.

    // Must be Async
    public void startComet(String serverUrl) {
        this.serverUrl = serverUrl;
        String gmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
        cometClient.start(serverUrl, gmsUser);
    }

    // Must be Async
    public void stopComet() {
        cometClient.disconnect();
    }

    public void startChat(String sessionId, String subject) {
        this.sessionId = sessionId;
        this.subject = subject;
        Log.d("GenesysChatController", "Starting chat service.");
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

    public void sendText(String text) {
        bus.post(new ChatSendEvent(sessionId, text, true));
    }

    public void startTyping() {
        bus.post(new ChatStartTypingEvent(sessionId, true));
    }

    public void stopTyping() {
        bus.post(new ChatStopTypingEvent(sessionId, true));
    }

    @Override
    public void onConnect() {
        // Bayeux client connected
        Log.d("GenesysChatController", "Comet client is connected.");
    }

    @Override
    public void onDisconnect() {
        // Bayeux client disconnected
        Log.d("GenesysChatController", "Comet client is disconnected.");
    }

    @Override @DebugLog
    public void onMessage(ClientSessionChannel channel, Message message) {
        ChatCometResponse chatCometResponse;
        try {
            Log.d("GenesysChatController", "Message from comet: " + message.getJSON());
            chatCometResponse = gson.fromJson(message.getJSON(), ChatCometResponse.class);
            Log.d("GenesysChatController", "Parsed ChatCometResponse: " + chatCometResponse);

            try {
                JSONObject jsonData = new JSONObject(message.getJSON());
                JSONObject jsonMessage = jsonData.getJSONObject("data").getJSONObject("message");
                ChatResponse chatResponse = gson.fromJson(jsonMessage.toString(), ChatResponse.class);
                Log.d("GenesysChatController", "Parsed ChatResponse: " + chatResponse);
            } catch(JSONException e) {
                Log.e("GenesysChatController", "Exception while parsing JSON chat response", e);
            }
        } catch (JsonSyntaxException e) {
            Log.e("GenesysChatController", "Exception while parsing Comet message", e);
            return;
        }
        ChatResponse chatResponse = null;
        try {
            chatResponse = chatCometResponse.getData().getMessage();
            try {
                Integer transcriptPosition = Integer.valueOf(chatResponse.getTranscriptPosition());
                if (transcriptPosition < cometClient.getTranscriptPosition()) {
                    Log.d("GenesysChatController", "Comet client is ahead of server!");
                }
                cometClient.setTranscriptPosition(transcriptPosition);
            } catch(NumberFormatException e) {
                Log.w("GenesysChatController", "Failed to parse transcript position", e);
            }
            List<TranscriptEntry> transcriptEntryList = chatResponse.getTranscriptToShow();
            if(transcriptEntryList != null) {
                for (TranscriptEntry entry : transcriptEntryList) {
                    Log.d("GenesysChatController", "Posting " + entry);
                    bus.post(new ChatTranscriptEvent(entry));
                }
            } else {
                Log.d("GenesysChatController", "No transcript to show!");
            }
        } catch (NullPointerException e) {
            Log.e("GenesysChatController", "Failed to process transcript entries: " + e);
        }
        if(chatResponse != null) {
            if(chatResponse.getChatIxnState() == ChatState.DISCONNECTED) {
                bus.post(new ChatResponseEvent(chatResponse, ChatRequestType.DISCONNECT));
            }
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
