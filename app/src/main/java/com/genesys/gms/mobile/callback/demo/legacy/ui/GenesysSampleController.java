package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.*;

import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v4.preference.PreferenceFragment;
import com.genesys.gms.mobile.callback.demo.legacy.common.ForActivity;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackDialog;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackQueuePosition;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmSyncMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackAvailabilityEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackCheckQueueEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackDialogEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.CallbackStartEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Pair;
import android.widget.Toast;
import timber.log.Timber;

import javax.inject.Inject;

public class GenesysSampleController {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final EventBus bus;

    private String sessionId;

    @Inject
	public GenesysSampleController(@ForActivity Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.bus = EventBus.getDefault();
	}

    public void persistState(Bundle outState) {
        outState.putString("sessionId", sessionId);
    }

    public void restoreState(Bundle inState) {
        sessionId = inState.getString("sessionId");
    }

	public void connect() {
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
        sessionId = dialog.getId();
        switch(dialog.getAction()) {
            case DIAL:
                String telUri = dialog.getTelUrl();
                String label = dialog.getLabel();
                Toast.makeText(context, label, Toast.LENGTH_SHORT).show();
                makeCall(Uri.parse(telUri));
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
                            Timber.i("User selected option [%s]: %s", label, url);
                            bus.post(new CallbackDialogEvent(url, false));
                        }
                    });
                builder.create().show();
                break;
            case CHAT:
                // Clean old persisted Chat information.
                sharedPreferences.edit()
                    .remove("CHAT_chatFinished")
                    .remove("CHAT_cometUrl")
                    .remove("CHAT_sessionId")
                    .remove("CHAT_subject")
                    .apply();
                Intent intent = new Intent(context, GenesysChatActivity.class);
                intent.setAction(Globals.ACTION_GENESYS_START_CHAT);
                // intent.putExtra(Globals.EXTRA_CHAT_URL, dialog.getStartChatUrl());
                intent.putExtra(Globals.EXTRA_COMET_URL, dialog.getCometUrl());
                intent.putExtra(Globals.EXTRA_SUBJECT, dialog.getChatParameters().getSubject());
                intent.putExtra(Globals.EXTRA_SESSION_ID, dialog.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            case CONFIRM:
                String text = dialog.getText();
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                break;
            default:
        }
    }

    @DebugLog
    public void handleGcmMessage(GcmSyncMessage gcmSyncMessage) {
        // Grab message and do post
        bus.post(new CallbackDialogEvent(gcmSyncMessage.getSyncUri(), true));
    }

    public void checkQueuePosition() {
        bus.post(new CallbackCheckQueueEvent(sessionId));
    }

    public void updateQueuePosition(CallbackQueuePosition callbackQueuePosition) {
        sharedPreferences.edit()
                .putString("queue_position", callbackQueuePosition.getPosition())
                .putString("queue_eta", callbackQueuePosition.getEta())
                .putBoolean("queue_agent_ready_threshold_passed", callbackQueuePosition.isAgentReadyThresholdPassed())
                .putString("queue_waiting", callbackQueuePosition.getTotalWaiting())
                .apply();
    }

	private void makeCall(Uri telUri) {
		boolean canCall = context.checkCallingOrSelfPermission("android.permission.CALL_PHONE") == PackageManager.PERMISSION_GRANTED;
		String action = canCall ? Intent.ACTION_CALL : Intent.ACTION_DIAL;
		context.startActivity(new Intent(action, telUri));
	}

	private void showError(String errorMessage) {
		new AlertDialog.Builder(context)
    	.setTitle("Genesys Service Error")
    	.setMessage("Received error:\n" + errorMessage)
	    .setNeutralButton(android.R.string.ok, null)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .show();
	}
	
	public void requestTimeSlots(String serviceName, String desiredTime) {
        DateTime desiredDateTime = TimeHelper.parseISO8601DateTime(desiredTime);
        DateTime startBound = desiredDateTime.minusHours(5);
        DateTime endBound = desiredDateTime.plusHours(5);
        bus.post(new CallbackAvailabilityEvent(serviceName, startBound, null, endBound, null));
	}
	
	public void updateTimeSlots(Map<DateTime, Integer> availability)
	{
        GenesysSampleActivity activity = (GenesysSampleActivity)context;
        PreferenceFragment callbackFragment = (PreferenceFragment)activity.getFragment(0);
        if (callbackFragment == null) {
            Timber.w("Unable to update time slots: Callback fragment is null.");
            return;
        }
        ListPreference selectedTime = (ListPreference)callbackFragment.findPreference("selected_time");
        if (selectedTime == null) {
            Timber.w("Unable to update time slots: selected_time Preference is null.");
            return;
        }
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
            if(availableSlot != null && availableSlot > 0) {
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
            desiredTime = sharedPreferences.getString("desired_time", null);
            closestTimeIndex = findClosestTime(newEntries, desiredTime);
            firstIndex = Math.max(closestTimeIndex - AVAILABLE_OPTIONS / 2, 0);
            lastIndex = Math.min(closestTimeIndex + AVAILABLE_OPTIONS / 2, newEntries.size() - 1);
            availableEntries = lastIndex - firstIndex + 1;

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
