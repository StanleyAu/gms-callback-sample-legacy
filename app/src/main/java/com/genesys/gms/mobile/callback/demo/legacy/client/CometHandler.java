package com.genesys.gms.mobile.callback.demo.legacy.client;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;

/**
 * Created by stau on 2/9/2015.
 * Used in conjunction with {@link com.genesys.gms.mobile.callback.demo.legacy.client.CometClient}
 */
public interface CometHandler {
    public void onConnect();
    public void onDisconnect();
    public void onMessage(ClientSessionChannel channel, Message message);
}
