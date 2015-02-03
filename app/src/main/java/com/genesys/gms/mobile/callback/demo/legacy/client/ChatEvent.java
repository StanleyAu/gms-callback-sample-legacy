package com.genesys.gms.mobile.callback.demo.legacy.client;

public class ChatEvent {
	public enum ChatEventType {
		PARTY_JOINED,
		PARTY_LEFT,
		MESSAGE,
		PUSH_URL,
		TYPING_STARTED,
		TYPING_STOPPED
	}
	
	public enum ChatPartyType {
		AGENT,
		CLIENT,
		EXTERNAL,
		SUPERVISOR
	}
	
	public final ChatEventType eventType;
	public final ChatPartyType partyType;
	public final int partyId;
	public final String nickname;
	public final String text;
	
	public ChatEvent(ChatEventType eventType, ChatPartyType partyType, int partyId, String nickname, String text) {
		super();
		this.eventType = eventType;
		this.partyType = partyType;
		this.partyId = partyId;
		this.nickname = nickname;
		this.text = text;
	}
}
