package com.genesys.gms.mobile.callback.demo.legacy.ui;

import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receives Google Cloud Messages and sends them as Intents to the {@link GenesysSampleActivity}.
 */
public class GenesysCloudMessageReceiver extends BroadcastReceiver {

	private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
	
	@Override
	public void onReceive(Context context, Intent intent) {
		StringBuilder logMessage = new StringBuilder();
		logMessage.append("Cloud message received: " + intent.getAction());
		Bundle extras = intent.getExtras();
		for (String key : extras.keySet()) {
			logMessage.append("\nCloud message extra: " + key + ": " + extras.get(key));
		}
		log.debug(logMessage.toString());
		
		Intent newIntent = new Intent(context, Globals.RECEIVER_ACTIVITY_CLASS);
		newIntent.setAction(Globals.ACTION_GENESYS_CLOUD_MESSAGE);
		newIntent.putExtra(Globals.EXTRA_MESSAGE, extras.getString("message"));
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(newIntent);
	}
}
