package com.genesys.gms.mobile.callback.demo.legacy.client;

public interface ChatListener {
	void chatEventReceived(ChatEvent chatEvent);
	void chatDisconnected();
}
