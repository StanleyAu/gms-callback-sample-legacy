package com.genesys.gms.mobile.callback.demo.legacy.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatSession {
	private final GenesysSession parentService;
	private final String createUrl;
	
	private String disconnectUrl;
	private String refreshFromStartUrl;
	private String refreshUrl;
	private String sendUrl;
	private String startTypingUrl;
	private String stopTypingUrl;
	
	public ChatSession(GenesysSession parentService, String createChatUrl) {
		this.parentService = parentService;
		this.createUrl = createChatUrl;
	}

	public void createInteraction(ChatCreationParameters chatParams) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("_verbose", "true"));
		params.add(new BasicNameValuePair("firstName", chatParams.firstName));
		params.add(new BasicNameValuePair("lastName", chatParams.lastName));
		params.add(new BasicNameValuePair("email", chatParams.email));
		params.add(new BasicNameValuePair("subject", chatParams.subject));
		params.add(new BasicNameValuePair("userDisplayName", chatParams.userDisplayName));
		params.addAll(chatParams.misc);
		if (chatParams.receiveCometEvents)
			params.add(new BasicNameValuePair("notify_by", "comet"));
		String response = parentService.post(createUrl, params);
		try {
			JSONObject responseJson = new JSONObject(response);
			disconnectUrl = responseJson.getString("_chatIxnAPI_DISCONNECT_URL");
			refreshFromStartUrl = responseJson.getString("_chatIxnAPI_REFRESH_FROM_START_URL");
			refreshUrl = responseJson.getString("_chatIxnAPI_REFRESH_URL");
			sendUrl = responseJson.getString("_chatIxnAPI_SEND_URL");
			startTypingUrl = responseJson.getString("_chatIxnAPI_START_TYPING_URL");
			stopTypingUrl = responseJson.getString("_chatIxnAPI_STOP_TYPING_URL");
			
			System.out.println(responseJson.toString(2));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void doAction(String actionUrl) {
		doAction(actionUrl, null);
	}
	
	private void doAction(String actionUrl, List<NameValuePair> params) {
		if (params == null)
			params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("_verbose", "true"));
		String response = parentService.post(actionUrl, params);
		try {
			JSONObject responseJson = new JSONObject(response);
			System.out.println(responseJson.toString(2));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void disconnect() {
		doAction(disconnectUrl);
	}
	
	public void refreshFromStart() {
		doAction(refreshFromStartUrl);		
	}
	
	public void refresh() {
		doAction(refreshUrl);		
	}
	
	public void send(String text) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("message", text));
		doAction(sendUrl, params);
	}
	
	public void startTyping() {
		doAction(startTypingUrl);
	}
	
	public void stopTyping() {
		doAction(stopTypingUrl);
	}

}
