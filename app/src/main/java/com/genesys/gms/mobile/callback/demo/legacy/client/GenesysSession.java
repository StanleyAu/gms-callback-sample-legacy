package com.genesys.gms.mobile.callback.demo.legacy.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent.ChatEventType;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent.ChatPartyType;

/**
 * Handles the communication with the Genesys Service.
 * 
 * Invocation methods in this class are blocking, because the underlying comet
 * implementation is blocking.
 */
public class GenesysSession {	
	private static final String GMS_USER_KEY = "gms_user";
	public enum HttpMethod
	{
		GET, POST, PUT, DELETE, HEAD
	}
	public interface ResponseHandler<T>
	{
		public T onSuccess(String response, ContentExchange exchange);
		public T onFailure(String response, ContentExchange exchange);
	}
	
	final Logger log;

	private final String serverUrl;
	private final String gmsUser;
	private final HttpClient httpClient;
	private final long requestTimeout;
	
	private BayeuxClient bayeuxClient;
	
	/**
	 * @param serverUrl Server URL like http://myserver:8080
	 * @param gmsUser gms_user Header to be used for this session.
	 * @param httpClient An HttpClient set up and started.
	 * @param loggerName Name of the logger used for logging.
	 */
	public GenesysSession(String serverUrl, String gmsUser, HttpClient httpClient, String loggerName, long requestTimeout) {
		this.serverUrl = serverUrl;
		this.gmsUser = gmsUser;
		this.httpClient = httpClient;
		this.log = LoggerFactory.getLogger(loggerName);
		this.requestTimeout = requestTimeout;
	}

	public void startComet(String cometUrl, final ChatListener chatListener, final Executor listenerExecutor) {
		if (bayeuxClient == null) {
			Map<String, Object> options = new HashMap<String, Object>();
			ClientTransport transport = new LongPollingTransport(options, httpClient) {
				@Override protected void customize(ContentExchange contentExchange) {
					super.customize(contentExchange);
					contentExchange.addRequestHeader(GMS_USER_KEY, gmsUser);
				}
			};

			bayeuxClient = new BayeuxClient(cometUrl, transport);
			bayeuxClient.handshake();
            /*
            bayeuxClient.getChannel(Channel.META_HANDSHAKE).addListener(new MessageListener() {
                @Override
                public void onMessage(ClientSessionChannel channel, Message message) {
                    if (message.isSuccessful()) {
                    }
                }
            });
            */

			boolean handshakeSuccess = bayeuxClient.waitFor(15000, BayeuxClient.State.CONNECTED);
			if (!handshakeSuccess)
				throw new RuntimeException("CometD handshake did not succeed");

			bayeuxClient.getChannel("/_genesys").subscribe(new MessageListener() {
				@Override public void onMessage(ClientSessionChannel channel, Message message) {
					try {
						cometMessageReceived(message, chatListener, listenerExecutor);
					} catch (Exception e) {
						log.error("Error handling comet message", e);
					}
				}
			});
		}
	}
	
	public void stopComet() {
		if (bayeuxClient != null) {
			bayeuxClient.disconnect();
		}
	}
	
	public void endSession() {
		stopComet();
	}
	
	protected void cometMessageReceived(Message message, final ChatListener chatListener, Executor listenerExecutor) {
		log.debug("Comet message received: " + message.getJSON());

		try {
			JSONObject jsonData = new JSONObject(message.getJSON());
			JSONObject jsonMessage = jsonData.getJSONObject("data").getJSONObject("message");
			
			JSONArray transcript = jsonMessage.optJSONArray("transcriptToShow");
			if (transcript != null) {
				for (int i = 0; i < transcript.length(); i++) {
					JSONArray transcriptItem = transcript.getJSONArray(i);
					final ChatEventType eventType = toChatEventType(transcriptItem.getString(0));
					final String nickname = transcriptItem.getString(1); 
					final String text = transcriptItem.getString(2);
					final int partyId = toPartyId(transcriptItem.getString(3));
					final ChatPartyType partyType = toChatPartyType(transcriptItem.getString(4));
					listenerExecutor.execute(new Runnable() {
						@Override public void run() {
							chatListener.chatEventReceived(new ChatEvent(eventType, partyType, partyId, nickname, text));
						}
					});
				}
			}

			if ("DISCONNECTED".equals(jsonMessage.getString("chatIxnState"))) {
				listenerExecutor.execute(new Runnable() {
					@Override public void run() {
						chatListener.chatDisconnected();
					}
				});
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ChatEventType toChatEventType(String eventType) {
		if ("Notice.TypingStarted".equals(eventType))
			return ChatEventType.TYPING_STARTED;
		else if ("Notice.TypingStopped".equals(eventType))
			return ChatEventType.TYPING_STOPPED;
		else if ("Notice.Left".equals(eventType))
			return ChatEventType.PARTY_LEFT;
		else if ("Notice.Joined".equals(eventType))
			return ChatEventType.PARTY_JOINED;
		else if ("Message.Text".equals(eventType)) {
			return ChatEventType.MESSAGE;
		} else {
			throw new RuntimeException("Unknown event type: " + eventType);
		}
	}
	
	private ChatPartyType toChatPartyType(String partyType) {
		try {
			return ChatPartyType.valueOf(partyType);
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException("Unknown chat party type: " + partyType);
		}
	}
	
	private int toPartyId(String partyId) {
		try {
			return Integer.parseInt(partyId);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Party id is not an integer: " + partyId);
		}
	}

	public ChatSession startChat(String urlPath) {
		return new ChatSession(this, urlPath);
	}
	
	/**
	 * @param urlPath Must start with a slash, for example "/genesys/1/service/request-interaction"
	 * @param params
	 * @return
	 */
	public String post(String urlPath, List<NameValuePair> params) {
		return (String)request(urlPath, params, HttpMethod.POST, null);
	}
	public Object post(String urlPath, List<NameValuePair> params, ResponseHandler<?> handler)
	{
		return request(urlPath, params, HttpMethod.POST, handler);
	}
	
	public String get(String urlPath, List<NameValuePair> params)
	{
		return (String)request(urlPath, params, HttpMethod.GET, null);
	}
	public Object get(String urlPath, List<NameValuePair> params, ResponseHandler<?> handler)
	{
		return request(urlPath, params, HttpMethod.GET, handler);
	}
	private Object request(String urlPath, List<NameValuePair> params, HttpMethod method, ResponseHandler<?> handler)
	{
		try {
			final AtomicReference<Throwable> exchangeException = new AtomicReference<Throwable>();

			ContentExchange exchange = new ContentExchange() {
				@Override
				protected void onConnectionFailed(Throwable e) {
					super.onConnectionFailed(e);
					exchangeException.set(e);
				}

				@Override
				protected void onException(Throwable e) {
					super.onException(e);
					exchangeException.set(e);
				}
			};

			exchange.setTimeout(requestTimeout);
			exchange.setRequestHeader(GMS_USER_KEY, gmsUser);

			String logMessage = "";
			switch(method)
			{

				case GET:
					exchange.setMethod("GET");
					if (params != null) {
						String query = URLEncodedUtils.format(params, "UTF-8");
						exchange.setURL(serverUrl + urlPath + "?" + query);
						logMessage = "Sending GET " + exchange.getRequestURI() +
							"\nHeader: " + GMS_USER_KEY + ": " + gmsUser;
					}
					break;
				case POST:
				default:
					exchange.setMethod("POST");
					exchange.setURL(serverUrl + urlPath);
					logMessage = "Sending POST " + exchange.getRequestURI() +
						"\nHeader: " + GMS_USER_KEY + ": " + gmsUser;
					exchange.setRequestContentType("application/x-www-form-urlencoded");
					if (params != null) {
						String content = URLEncodedUtils.format(params, "UTF-8");
						exchange.setRequestContent(new ByteArrayBuffer(content, "UTF-8"));
						logMessage += "\nContent: " + content;
					}
			}

			log.debug(logMessage);

			httpClient.send(exchange);

			exchange.waitForDone();

			String response = exchange.getResponseContent();
			int responseStatus = exchange.getResponseStatus();

			log.debug("Response: " + responseStatus + " " + response);

			if (exchange.getStatus() != HttpExchange.STATUS_COMPLETED)
				throw new RuntimeException("HTTP exchange not completed", exchangeException.get());

			if (handler == null)
			{
				handler = new ResponseHandler<String>(){
					public String onSuccess(String response, ContentExchange exchange)
					{
						return response;
					}
					public String onFailure(String response, ContentExchange exchange)
					{
						String errorMessage = null;
						if (response != null) {
							try {
								JSONObject responseJson = new JSONObject(response);
								errorMessage = responseJson.getString("message");
							} catch (JSONException noErrorMessageObtained) {}
						}

						if (errorMessage == null) {
							String truncatedResponse = response.length() > 1024 ?
								"[truncated]" + response.substring(0, 1024) :
								response;
							throw new RuntimeException("HTTP exchange failed. Response status " + exchange.getResponseStatus() +
								"\nResponse content:\n" + truncatedResponse);
						} else {
							throw new RuntimeException(errorMessage);
						}
					}
				};
			}

			Object result;
			if (!HttpStatus.isSuccess(responseStatus)) {
				result = handler.onFailure(response, exchange);
			}
			else
			{
				result = handler.onSuccess(response, exchange);
			}

			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
