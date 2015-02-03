package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesys.gms.mobile.callback.demo.legacy.ui.GenesysService.RequestType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Pair;
import android.widget.Toast;

public class GenesysController {

	private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
	private final GenesysService genesysService;

	// TODO: Move code to utility class
	final private static String ISO8601_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";
	final private static DateTimeFormatter ISO8601_FORMATTER = DateTimeFormat.forPattern(ISO8601_FORMAT).withZone(DateTimeZone.UTC);
	
	private String sessionId;

	public GenesysController(GenesysService genesysService) {
		//this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.genesysService = genesysService;
	}

	public void connect(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String serverUrl = sharedPreferences.getString("server_url", null);
		String urlPath = sharedPreferences.getString("url_path", null);
		String serviceName = sharedPreferences.getString("service_name", null);
		String gmsUser = sharedPreferences.getString("gms_user", null);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("first_name", sharedPreferences.getString("first_name", null)));
		params.add(new BasicNameValuePair("last_name", sharedPreferences.getString("last_name", null)));
		params.add(new BasicNameValuePair("_provide_code", Boolean.toString(sharedPreferences.getBoolean("provide_code", false))));
		params.add(new BasicNameValuePair("_customer_number", sharedPreferences.getString("this_phone_number", null)));
		
		boolean registerCloudMessaging = sharedPreferences.getBoolean("push_notifications_enabled", false);
		boolean useCallbackInterface = true;

		String scenario = sharedPreferences.getString("scenario", null);
		if (scenario.equals("VOICE-NOW-USERORIG")) {
			params.add(new BasicNameValuePair("_call_direction", "USERORIGINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "false"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "false"));
			params.add(new BasicNameValuePair("_media_type", "voice"));
		} else if (scenario.equals("VOICE-WAIT-USERORIG")) {
			params.add(new BasicNameValuePair("_call_direction", "USERORIGINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "true"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "true"));
			params.add(new BasicNameValuePair("_media_type", "voice"));
		} else if (scenario.equals("VOICE-NOW-USERTERM")) {
			params.add(new BasicNameValuePair("_call_direction", "USERTERMINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "false"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "false"));
			params.add(new BasicNameValuePair("_media_type", "voice"));
		} else if (scenario.equals("VOICE-WAIT-USERTERM")) {
			params.add(new BasicNameValuePair("_call_direction", "USERTERMINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "true"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "true"));
			params.add(new BasicNameValuePair("_media_type", "voice"));
		} else if (scenario.equals("VOICE-SCHEDULED-USERTERM")) {
			//useCallbackInterface = true;
			params.add(new BasicNameValuePair("_call_direction", "USERTERMINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "true"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "true"));
			params.add(new BasicNameValuePair("_media_type", "voice"));
			
			String desiredTime = sharedPreferences.getString("selected_time", null);
			params.add(new BasicNameValuePair("_desired_time", desiredTime));
		} else if (scenario.equals("CHAT-NOW")) {
			params.add(new BasicNameValuePair("_call_direction", "USERORIGINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "false"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "false"));
			params.add(new BasicNameValuePair("_media_type", "chat"));
		} else if (scenario.equals("CHAT-WAIT")) {
			params.add(new BasicNameValuePair("_call_direction", "USERORIGINATED"));
			params.add(new BasicNameValuePair("_wait_for_agent", "true"));
			params.add(new BasicNameValuePair("_wait_for_user_confirm", "true"));
			params.add(new BasicNameValuePair("_media_type", "chat"));
		} else { // CUSTOM
		}
		
		genesysService.startSession(serverUrl, urlPath, serviceName, gmsUser, params, registerCloudMessaging, useCallbackInterface);
	}

	public void handleIntent(Context context, Intent intent) {		
		boolean isErrorMessage = Globals.ACTION_GENESYS_ERROR_MESSAGE.equals(intent.getAction());
		if (isErrorMessage) {
			showError(context, intent.getStringExtra(Globals.EXTRA_MESSAGE));
		}
		
		boolean isResponse = Globals.ACTION_GENESYS_RESPONSE.equals(intent.getAction());
		boolean isCloudMessage = Globals.ACTION_GENESYS_CLOUD_MESSAGE.equals(intent.getAction());		
		if (isResponse || isCloudMessage) {
			String message = intent.getExtras().getString(Globals.EXTRA_MESSAGE);
			try {
				JSONObject messageJson = new JSONObject(message);
				
				if (isResponse) {
					RequestType type = (RequestType)intent.getExtras().getSerializable(Globals.EXTRA_REQUEST_TYPE);
					interpretResponse(context, messageJson, type);
				}
				else
				{
					interpretCloudMessage(context, messageJson);
				}
			} 
			catch (Exception e) {
				log.error("Wrong response", e);
				showError(context, "Unexpected response from the Genesys server, see logs");
			}
		}
	}

	private void makeCall(Context context, Uri telUri) {
		boolean canCall = context.checkCallingOrSelfPermission("android.permission.CALL_PHONE") == PackageManager.PERMISSION_GRANTED;
		String action = canCall ? Intent.ACTION_CALL : Intent.ACTION_DIAL;
		context.startActivity(new Intent(action, telUri));
	}
	
	private void interpretResponse(Context context, JSONObject response, RequestType type) throws JSONException {
		if (type == RequestType.AVAILABILITY)
		{
			updateTimeSlots(context, response);
		}
		else if (response.has("error")) {
			showError(context, response.getString("error"));
		}
		else {
			if (response.has("_id")) {
				sessionId = response.getString("_id");
			}

			String action = response.optString("_action", null);
	
			if ("DialNumber".equals(action)) {
				String telUri = response.getString("_tel_url");
				makeCall(context, Uri.parse(telUri));
			}
			else if ("DisplayMenu".equals(action)) {
				String label = response.getString("_label");
				JSONArray content = response.getJSONArray("_content");
				JSONObject group = content.getJSONObject(0);
				String groupName = group.getString("_group_name");
				final JSONArray groupContent = group.getJSONArray("_group_content");
				String[] menuItems = new String[groupContent.length()];
				for (int i = 0; i < groupContent.length(); i++) {
					menuItems[i] = groupContent.getJSONObject(i).getString("_label");
				}
				
			    AlertDialog.Builder builder = new AlertDialog.Builder(context);
			    builder.setTitle(label + "\n" + groupName)
		           .setItems(menuItems, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
							try {
								String label = groupContent.getJSONObject(which).getString("_label");
								String url = groupContent.getJSONObject(which).getString("_user_action_url");
								Log.i("GenesysController", "User selected option [" + label + "]: " + url);
								genesysService.continueDialog(url);
							} catch (JSONException e) {
								throw new RuntimeException(e);
							}
		               }
		           });
			    builder.create().show();
			}
			else if ("StartChat".equals(action)) {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				
				Intent intent = new Intent(context, GenesysChatActivity.class);
				intent.setAction(Globals.ACTION_GENESYS_START_CHAT);
				intent.putExtra(Globals.EXTRA_CHAT_URL, response.getString("_start_chat_url"));
				intent.putExtra(Globals.EXTRA_COMET_URL, response.getString("_comet_url"));
				intent.putExtra(Globals.EXTRA_SUBJECT, response.getJSONObject("_chat_parameters").getString("subject"));
				context.startActivity(intent);
			}
			else if (("ConfirmationDialog".equals(action) || action == null) && response.has("_text")) {
				String text = response.getString("_text");
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void interpretCloudMessage(Context context, JSONObject message) throws JSONException {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String id = message.getString("_id");
		String action = message.getString("_action");
		String urlHostPort = sharedPreferences.getString("server_url", null);
		String servicePath = sharedPreferences.getString("url_path", null);
		String url = urlHostPort + servicePath  + id + "/" + action;
		genesysService.continueDialog(url);
	}
	
	private void showError(Context context, String errorMessage) {
		new AlertDialog.Builder(context)
    	.setTitle("Genesys Service Error")
    	.setMessage("Received error:\n" + errorMessage)
	    .setNeutralButton(android.R.string.ok, null)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .show();
	}

	public void refreshQueue() {
		if (sessionId != null) {
			genesysService.post("/" + sessionId + "/check-queue-position", null);
		}
	}
	
	public void requestTimeSlots(String serviceName, String desiredTime)
	{
		DateTime desiredDateTime = ISO8601_FORMATTER.parseDateTime(desiredTime);
		DateTime startBound = desiredDateTime.minusHours(5);
		DateTime endBound = desiredDateTime.plusHours(5);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("start",startBound.toString(ISO8601_FORMAT)));
		params.add(new BasicNameValuePair("end",endBound.toString(ISO8601_FORMAT)));
		Log.d("requestTimeSlots()", "Availability params: " + params);
		genesysService.post("/callback/" + serviceName + "/availability", params, RequestType.AVAILABILITY);
	}
	
	public void updateTimeSlots(Context context, JSONObject response)
	{
		if(!(context instanceof GenesysSampleActivity))
		{
			return;
		}
		GenesysSampleActivity activity = (GenesysSampleActivity)context;
		PreferenceFragment callbackFragment = (PreferenceFragment)activity.tabs[0].fragment;
		ListPreference selectedTime = (ListPreference)callbackFragment.findPreference("selected_time");

		if(response.has("exception"))
		{
			try
			{
				String exception = response.getString("exception");
				if(exception.endsWith("CallbackExceptionResource"))
				{
					new AlertDialog.Builder(context)
						.setTitle("Error")
						.setMessage("Service not configured!")
						.setCancelable(false)
						.setPositiveButton("Dismiss",null)
						.show();
					selectedTime.setSummary("No office hours found");
				}
				else if(exception.endsWith("CallbackExceptionAvailability"))
				{
					// Office closed
					selectedTime.setSummary("Office closed at desired time");
				}
			}
			catch(JSONException exc){}
			selectedTime.setEntries(R.array.empty);
			selectedTime.setEntryValues(R.array.empty);
		}
		else
		{
			List<Pair<String,String>> newEntries = new ArrayList<Pair<String,String>>();
			@SuppressWarnings("unchecked")
			Iterator<String> keys = response.keys();
			String timeSlot;
			DateTime timeSlotAsDateTime;
			int availableSlots = 0;
			while(keys.hasNext())
			{
				timeSlot = keys.next();
				timeSlotAsDateTime = TimeHelper.parseISO8601DateTime(timeSlot);
				if(timeSlotAsDateTime.isBeforeNow()) {
					// Filter out timeslots that have already passed
					continue;
				}
				try
				{
					availableSlots = response.getInt(timeSlot);
					if(availableSlots>0)
					{
						newEntries.add(new Pair<String,String>(
								TimeHelper.toFriendlyString(timeSlot), timeSlot
								));
					}
				}
				catch(JSONException exc)
				{
					showError(context, exc.getMessage());
				}
			}
			if(newEntries.size()==0)
			{
				selectedTime.setSummary("No time slots available");
				selectedTime.setEntries(R.array.empty);
				selectedTime.setEntryValues(R.array.empty);
			}
			else
			{
				String desiredTime;
				int closestTimeIndex;
				int firstIndex;
				int lastIndex;
				int availableEntries;
				final int AVAILABLE_OPTIONS = 5;
				selectedTime.setSummary("Tap to select");
				Collections.sort(newEntries, new Comparator<Pair<String,String>>() {
					@Override
					public int compare(Pair<String,String> pair1, Pair<String,String> pair2)
					{
						return pair1.second.compareTo(pair2.second);
					}
				});

				// binary search
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				desiredTime = sharedPreferences.getString("desired_time", null);
				closestTimeIndex = findClosestTime(newEntries, desiredTime);
				firstIndex = Math.max(closestTimeIndex - AVAILABLE_OPTIONS / 2, 0);
				lastIndex = Math.min(closestTimeIndex + AVAILABLE_OPTIONS / 2, newEntries.size() - 1);
				availableEntries = lastIndex - firstIndex + 1;
				Log.d("updateTimeSlots()", "closestTimeIndex: " + closestTimeIndex + " firstIndex: " + firstIndex + " lastIndex: " + lastIndex);

				CharSequence[] entriesList = new CharSequence[availableEntries];
				CharSequence[] entryValuesList = new CharSequence[availableEntries];
				int i = 0;
				for(int j=firstIndex;j<=lastIndex;++j)
				{
					entriesList[i] = newEntries.get(j).first;
					entryValuesList[i] = newEntries.get(j).second;
					++i;
				}
				selectedTime.setEntries(entriesList);
				selectedTime.setEntryValues(entryValuesList);
			}
		}
		selectedTime.setEnabled(true);
	}

	private int findClosestTime(List<Pair<String, String>> availableTimes, String desiredTime) {
		int lo = 0;
		int hi;
		int mid = -1;
		int result;
		if(availableTimes == null || availableTimes.isEmpty() || desiredTime == null) {
			return -1;
		}
		hi = availableTimes.size() - 1;
		while(lo <= hi) {
			mid = lo + (hi - lo) / 2;
			result = desiredTime.compareTo(availableTimes.get(mid).second);
			Log.d("findClosestTime()", "lo: " + lo + " mid: " + mid + " hi: " + hi + " result: " + result + " desiredTime: " + desiredTime + " availableTime: " + availableTimes.get(mid).second);
			if (result < 0) {
				// less than mid
				hi = mid - 1;
			} else if (result > 0) {
				// greater than mid
				lo = mid + 1;
			} else {
				// equal, use this
				return mid;
			}
		}
		return mid;
	}
}
