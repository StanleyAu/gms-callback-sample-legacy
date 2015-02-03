package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.genesys.gms.mobile.callback.demo.legacy.client.ChatListener;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatSession;
import com.genesys.gms.mobile.callback.demo.legacy.client.GenesysSession;
import com.genesys.gms.mobile.callback.demo.legacy.client.GenesysSession.ResponseHandler;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GenesysService extends Service {
	
	private static final String LOG_TAG = Globals.GENESYS_LOG_TAG;
	private final Logger log = LoggerFactory.getLogger(LOG_TAG);
	
	private final HttpClient httpClient;
	private final ExecutorService networkingExecutor = Executors.newSingleThreadExecutor();

	public enum RequestType {
		NONE, AVAILABILITY, CHECK_QUEUE
	};
	
	private GenesysSession genesysSession;
	
	public GenesysService() {
		httpClient = new HttpClient();
		httpClient.setConnectTimeout(Globals.CONNECT_TIMEOUT);
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(3);
		threadPool.setMaxThreads(3); // minimum required is 3
		threadPool.setDaemon(true);
		threadPool.setName(GenesysService.class.getSimpleName() + ".httpClient");
		httpClient.setThreadPool(threadPool);
		try {
			httpClient.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onDestroy() {
		if (genesysSession != null)
			genesysSession.endSession();

		networkingExecutor.shutdown();

		try {
			httpClient.stop();
		} catch (Exception e) {
			log.error("Error on stopping httpClient", e);
		}
		httpClient.destroy();

		super.onDestroy();
	}
	
	public class LocalBinder extends Binder {
		public GenesysService getService() {
			return GenesysService.this;
		}
	}
	
	private final IBinder serviceBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void startSession(
			final String serverUrl,
			final String urlPath,
			final String serviceName,
			final String gmsUser,
			final List<NameValuePair> params,
			final boolean registerCloudMessaging,
			final boolean useCallbackInterface) {
		endCurrentSession();

		executeAndNotifyError(new Runnable() {
			@Override public void run() {
				List<NameValuePair> params2 = params;
				if (registerCloudMessaging) {
					String registrationId = registerGoogleCloudMessaging();
					if (params2 == null)
						params2 = new ArrayList<NameValuePair>();
					params2.add(new BasicNameValuePair("_device_notification_id", registrationId));
					params2.add(new BasicNameValuePair("_device_os", "gcm"));
				}
				
				genesysSession = new GenesysSession(serverUrl, gmsUser, httpClient, LOG_TAG, Globals.REQUEST_TIMEOUT);
				if(useCallbackInterface)
				{
					postImpl(urlPath + "callback/" + serviceName, params2, RequestType.NONE);
				}
				else
				{
					postImpl(urlPath + serviceName, params2, RequestType.NONE);
				}
			}
		});
	}
	
	public void endCurrentSession() {
		if (genesysSession != null) {
			genesysSession.endSession();
			genesysSession = null;
		}
	}
	
	public void continueDialog(String url) {
		Log.i("GenesysService", "Continuing dialog with POST " + url);
		post(Uri.parse(url).getPath(), null);
	}
	
	public ChatSession startChat(String chatUrl, final String cometUrl,
			final ChatListener chatListener, final Executor listenerExecutor) {
		
		executeAndNotifyError(new Runnable() {
			@Override public void run() {
				genesysSession.startComet(cometUrl, chatListener, listenerExecutor);
			}
		});
		
		ChatSession chatSession = genesysSession.startChat(Uri.parse(chatUrl).getPath());
		return chatSession;
	}

	public void execute(final Runnable r) {
		networkingExecutor.execute(new Runnable() {
			@Override public void run() {
				r.run();
			}
		});
	}
	
	public void post(final String urlPath, final List<NameValuePair> params)
	{
		post(urlPath, params, RequestType.NONE);
	}
		
	public void post(final String urlPath, final List<NameValuePair> params, final RequestType type) {
		executeAndNotifyError(new Runnable() {
			@Override public void run() {
				postImpl(urlPath, params, type);
			}
		});
	}
	
	private void executeAndNotifyError(final Runnable r) {
		networkingExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					r.run();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					
					StringBuilder message = new StringBuilder();
					message.append(e.toString());
					Throwable cause = e.getCause();
					while (cause != null) {
						message.append("\ncaused by " + cause.toString());
						cause = cause.getCause();
					}
					
					Intent intent = new Intent(GenesysService.this, Globals.RECEIVER_ACTIVITY_CLASS);
					intent.setAction(Globals.ACTION_GENESYS_ERROR_MESSAGE);
					intent.putExtra(Globals.EXTRA_MESSAGE, message.toString());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
		});
	}

	private void postImpl(String urlPath, List<NameValuePair> params, RequestType type) {
		String response = "";

		if(type==RequestType.AVAILABILITY)
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String serverUrl = sharedPreferences.getString("server_url", null);
			String servicePath = sharedPreferences.getString("url_path", null);
			String gmsUser = sharedPreferences.getString("gms_user", null);
			GenesysSession tempSession = new GenesysSession(serverUrl + servicePath, gmsUser, httpClient, LOG_TAG, Globals.REQUEST_TIMEOUT);
			response = (String)tempSession.get(urlPath, params, new ResponseHandler<String>(){
				public String onSuccess(String response, ContentExchange exchange)
				{
					return response;
				}
				public String onFailure(String response, ContentExchange exchange)
				{
					if(exchange.getResponseStatus()!=400)
					{
						log.warn(LOG_TAG, "Unexpected response received: " + response);
					}
					return response;
				}
			});
		}
		else
		{
			response = genesysSession.post(urlPath, params);
		}

		Intent intent = new Intent(GenesysService.this, Globals.RECEIVER_ACTIVITY_CLASS);
		intent.setAction(Globals.ACTION_GENESYS_RESPONSE);
		intent.putExtra(Globals.EXTRA_MESSAGE, response);
		intent.putExtra(Globals.EXTRA_REQUEST_TYPE, type);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	private String registerGoogleCloudMessaging() {
		try {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String gcmSenderId = sharedPreferences.getString("gcm_sender_id", null);
			String registrationId = GoogleCloudMessaging.getInstance(this).register(gcmSenderId);
			log.debug(LOG_TAG, "Registered for cloud messages. Sender id: " + gcmSenderId);
			return registrationId;
		} catch (IOException e) {
			throw new RuntimeException("Unable to register for cloud messages", e);
		}
	}
	
}
