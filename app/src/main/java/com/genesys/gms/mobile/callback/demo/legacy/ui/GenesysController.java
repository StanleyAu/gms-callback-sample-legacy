package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.*;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackDialog;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackAvailabilityEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackStartEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.*;
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
    private final Context context;
	private final GenesysService genesysService;
    private final EventBus bus;
	private String sessionId;

    @DebugLog
	public GenesysController(Context context, GenesysService genesysService, EventBus bus) {
		//this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //The GenesysService is literally only used for HTTP, must be deprecated
        this.context = context;
		this.genesysService = genesysService;
        this.bus = bus;
	}

	public void connect(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> params = new HashMap<String, String>();
        String strDesiredTime = null;
        CallbackStartEvent event = null;

        params.put("first_name", sharedPreferences.getString("first_name", null));
        params.put("last_name", sharedPreferences.getString("last_name", null));
        params.put("_provide_code", Boolean.toString(sharedPreferences.getBoolean("provide_code", false)));
        params.put("_customer_number", sharedPreferences.getString("this_phone_number", null));

        // We've already confirmed that GCM is registered if needed
        boolean registerCloudMessaging = sharedPreferences.getBoolean("push_notifications_enabled", false);
        if (registerCloudMessaging) {
            params.put("_device_notification_id", sharedPreferences.getString(GcmManager.PROPERTY_REG_ID, null));
            params.put("_device_os", "gcm");
        }

        String scenario = sharedPreferences.getString("scenario", null);
        if (scenario.equals("VOICE-NOW-USERORIG")) {
            params.put("_call_direction", "USERORIGINATED");
            params.put("_wait_for_agent", "false");
            params.put("_wait_for_user_confirm", "false");
            params.put("_media_type", "voice");
        } else if (scenario.equals("VOICE-WAIT-USERORIG")) {
            params.put("_call_direction", "USERORIGINATED");
            params.put("_wait_for_agent", "true");
            params.put("_wait_for_user_confirm", "true");
            params.put("_media_type", "voice");
        } else if (scenario.equals("VOICE-NOW-USERTERM")) {
            params.put("_call_direction", "USERTERMINATED");
            params.put("_wait_for_agent", "false");
            params.put("_wait_for_user_confirm", "false");
            params.put("_media_type", "voice");
        } else if (scenario.equals("VOICE-WAIT-USERTERM")) {
            params.put("_call_direction", "USERTERMINATED");
            params.put("_wait_for_agent", "true");
            params.put("_wait_for_user_confirm", "true");
            params.put("_media_type", "voice");
        } else if (scenario.equals("VOICE-SCHEDULED-USERTERM")) {
            params.put("_call_direction", "USERTERMINATED");
            strDesiredTime = sharedPreferences.getString("selected_time", null);
            params.put("_wait_for_agent", "true");
            params.put("_wait_for_user_confirm", "true");
            params.put("_media_type", "voice");
        } else if (scenario.equals("CHAT-NOW")) {
            params.put("_call_direction", "USERORIGINATED");
            params.put("_wait_for_agent", "false");
            params.put("_wait_for_user_confirm", "false");
            params.put("_media_type", "chat");
        } else if (scenario.equals("CHAT-WAIT")) {
            params.put("_call_direction", "USERORIGINATED");
            params.put("_wait_for_agent", "true");
            params.put("_wait_for_user_confirm", "true");
            params.put("_media_type", "chat");
        } else { // CUSTOM
        }

        bus.post(new CallbackStartEvent(
            sharedPreferences.getString("service_name", null),
            sharedPreferences.getString("_customer_number", null),
            strDesiredTime,
            null, // _callback_state
            null, // _urs_virtual_queue
            null, // _request_queue_time_stat
            params
        ));
	}

    public void handleDialog(CallbackDialog dialog) {
        switch(dialog.getAction()) {
            case DIAL:
                String telUri = dialog.getTelUrl();
                String label = dialog.getLabel();
                Toast.makeText(context, label, Toast.LENGTH_SHORT).show();
                makeCall(context, Uri.parse(telUri));
                break;
            case MENU:
                if(dialog.getContent() == null || dialog.getContent().size() == 0) {
                    // It's empty! No menu to show...
                    break;
                }
                CallbackDialog.DialogGroup group = dialog.getContent().get(0);
                String groupName = group.getGroupName();
                final List<CallbackDialog.GroupContent> groupContents = group.getGroupContent();
                if(groupContents == null || groupContents.size() == 0) {
                    // It's empty! There are no options...
                    break;
                }
                String[] menuItems = new String[groupContents.size()];
                for (int i = 0; i < groupContents.size(); i++) {
                    menuItems[i] = groupContents.get(i).getLabel();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(dialog.getLabel() + "\n" + groupName)
                    .setItems(menuItems, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String label = groupContents.get(which).getLabel();
                            String url = groupContents.get(which).getUserActionUrl();
                            Log.i("GenesysController", "User selected option [" + label + "]: " + url);
                            // This just means POST
                            genesysService.continueDialog(url);
                        }
                    });
                builder.create().show();
                break;
            case CHAT:
                Intent intent = new Intent(context, GenesysChatActivity.class);
                intent.setAction(Globals.ACTION_GENESYS_START_CHAT);
                intent.putExtra(Globals.EXTRA_CHAT_URL, dialog.getStartChatUrl());
                intent.putExtra(Globals.EXTRA_COMET_URL, dialog.getCometUrl());
                intent.putExtra(Globals.EXTRA_SUBJECT, dialog.getChatParameters().getSubject());
                context.startActivity(intent);
                break;
            case CONFIRM:
                String text = dialog.getText();
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                break;
            default:
        }
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
		if (response.has("error")) {
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
	
	public void requestTimeSlots(String serviceName, String desiredTime) {
        DateTime desiredDateTime = TimeHelper.parseISO8601DateTime(desiredTime);
        DateTime startBound = desiredDateTime.minusHours(5);
        DateTime endBound = desiredDateTime.plusHours(5);
        bus.post(new CallbackAvailabilityEvent(serviceName, startBound, null, endBound, null));
	}
	
	public void updateTimeSlots(Context context, Map<DateTime, Integer> availability)
	{
        GenesysSampleActivity activity = (GenesysSampleActivity)context;
        PreferenceFragment callbackFragment = (PreferenceFragment)activity.tabs[0].fragment;
        ListPreference selectedTime = (ListPreference)callbackFragment.findPreference("selected_time");
        List<Pair<String,String>> newEntries = new ArrayList<Pair<String,String>>();

        DateTime timeSlot;
        Integer availableSlot;
        String friendlyTime;
        String strTimeSlot;

        for(Map.Entry<DateTime, Integer> entry : availability.entrySet()) {
            timeSlot = entry.getKey();
            availableSlot = entry.getValue();
            if(entry.getKey().isBeforeNow()) {
                continue;
            }
            if(entry.getValue() != null && entry.getValue() > 0) {
                friendlyTime = TimeHelper.toFriendlyString(timeSlot);
                strTimeSlot = TimeHelper.serializeUTCTime(timeSlot);
                newEntries.add(new Pair<String, String>(
                    friendlyTime, strTimeSlot
                ));
            }
        }
        if(newEntries.size()==0)
        {
            selectedTime.setSummary("No time slots available");
            selectedTime.setEntries(R.array.empty);
            selectedTime.setEntryValues(R.array.empty);
        } else {
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
        selectedTime.setEnabled(true);
        /*
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
		*/
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
