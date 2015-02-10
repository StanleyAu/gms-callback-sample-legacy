package com.genesys.gms.mobile.callback.demo.legacy.client;

import android.util.Log;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stau on 2/9/2015.
 */
public class CometClient {
    public static final String GMS_USER = "gms_user";
    private final HttpClient httpClient;
    private BayeuxClient bayeuxClient;
    private CometHandler handler;
    private int transcriptPosition = 0;

    private boolean connected = false;

    public CometClient(HttpClient httpClient, CometHandler handler) {
        this.httpClient = httpClient;
        this.handler = handler;
    }

    // TODO: This kind of logic should be removed from a generic Comet client wrapper
    public int getTranscriptPosition() {
        return transcriptPosition;
    }

    // TODO: This kind of logic should be removed from a generic Comet client wrapper
    public void setTranscriptPosition(int transcriptPosition) {
        this.transcriptPosition = transcriptPosition;
    }

    public void start(String serverUrl, final String gmsUser) {
        if(bayeuxClient != null && bayeuxClient.isConnected()) {
            // TODO: Bayeux Client is unexpectedly already connected?
            return;
        }

        if(bayeuxClient == null) {
            Map<String, Object> options = new HashMap<String, Object>();
            ClientTransport transport = new LongPollingTransport(options, httpClient) {
                @Override
                protected void customize(ContentExchange contentExchange) {
                    super.customize(contentExchange);
                    if (gmsUser != null && !gmsUser.isEmpty()) {
                        Log.d("CometClient", "Adding " + GMS_USER + ": " + gmsUser + " header.");
                        contentExchange.addRequestHeader(GMS_USER, gmsUser);
                    }
                }
            };
            bayeuxClient = new BayeuxClient(serverUrl, transport);
            //addListeners();
            addExtensions();
        }
        if(!bayeuxClient.isHandshook()) {
            bayeuxClient.handshake();

            boolean handshakeSuccess = bayeuxClient.waitFor(15000, BayeuxClient.State.CONNECTED);
            if (!handshakeSuccess)
                throw new RuntimeException("CometD handshake did not succeed");

            bayeuxClient.getChannel("/_genesys").subscribe(new ClientSessionChannel.MessageListener() {
                @Override public void onMessage(ClientSessionChannel channel, Message message) {
                    try {
                        handler.onMessage(channel, message);
                    } catch (Exception e) {
                        Log.e("CometClient", "Error handling comet message", e);
                    }
                }
            });
        }
    }

    protected void addListeners() {
        bayeuxClient.batch(new Runnable() {
            @Override
            public void run() {
                bayeuxClient.getChannel(Channel.META_HANDSHAKE).addListener(new ClientSessionChannel.MessageListener() {
                    @Override
                    public void onMessage(ClientSessionChannel channel, Message message) {
                        if (message.isSuccessful()) {
                            Log.d("CometClient", "Handshake successful, adding subs");
                            addSubscriptions();
                        }
                    }
                });
                bayeuxClient.getChannel(Channel.META_CONNECT).addListener(new ClientSessionChannel.MessageListener() {
                    @Override
                    public void onMessage(ClientSessionChannel channel, Message message) {
                        if(bayeuxClient.isDisconnected()) {
                            return;
                        }
                        boolean wasConnected = connected;
                        connected = message.isSuccessful();
                        if(!wasConnected && connected) {
                            handler.onConnect();
                        }
                        else if(wasConnected && !connected) {
                            handler.onDisconnect();
                        }
                    }
                });
                bayeuxClient.getChannel(Channel.META_DISCONNECT).addListener(new ClientSessionChannel.MessageListener() {
                    @Override
                    public void onMessage(ClientSessionChannel channel, Message message) {
                        if(message.isSuccessful()) {
                            connected = false;
                            // TODO: Does this occur before/after META_CONNECT message?
                        }
                    }
                });
            }
        });
    }

    protected void addSubscriptions() {
        // Perform channel subscriptions here
        // Call hook to perform subscriptions
        bayeuxClient.batch(new Runnable() {
            @Override
            public void run() {
                bayeuxClient.getChannel("/_genesys").subscribe(new ClientSessionChannel.MessageListener() {
                    @Override public void onMessage(ClientSessionChannel channel, Message message) {
                        handler.onMessage(channel, message);
                    }
                });
            }
        });
    }

    protected void addExtensions() {
        bayeuxClient.addExtension(new ClientSession.Extension.Adapter() {
            @Override
            public boolean sendMeta(ClientSession session, Message.Mutable message) {
                if(Channel.META_DISCONNECT.equals(message.getChannel())) {
                    Log.d("CometClient", "Inserting transcriptPosition ext");
                    Map<String,Object> ext = message.getExt(true);
                    // TODO: Use hook to retrieve value
                    ext.put("transcriptPosition", transcriptPosition);
                }
                return true;
            }
        });
    }

    public void disconnect() {
        if(bayeuxClient != null && !bayeuxClient.isDisconnected()) {
            bayeuxClient.disconnect();
        }
    }
}
