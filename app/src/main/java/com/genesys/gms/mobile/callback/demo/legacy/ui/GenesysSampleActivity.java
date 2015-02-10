package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.DialogFragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmSyncMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.UnknownErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsRequestInterceptor;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

// TODO: Possibly redesign how the communication from service to activity is done. Now it is done with explicit intents, configuring the activity as singleTask.
public class GenesysSampleActivity extends AbstractTabActivity implements OnSharedPreferenceChangeListener {
	private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
    public static final CharSequence[] EMPTY_LIST = {};

    @Inject SharedPreferences sharedPreferences;
    @Inject GmsEndpoint gmsEndpoint;
    @Inject GmsRequestInterceptor gmsRequestInterceptor;
    @Inject Gson gson;
	@Inject GenesysSampleController controller;
    private final EventBus bus;
    private Menu menu;

	@DebugLog
	public GenesysSampleActivity() {
        this.bus = EventBus.getDefault();
	}

    private void tryUpdateEndpoint() {
        String strHost = sharedPreferences.getString(Globals.PROPERTY_HOST, null);
        String strPort = sharedPreferences.getString(Globals.PROPERTY_PORT, null);
        String strApiVersion = sharedPreferences.getString(Globals.PROPERTY_API_VERSION, null);
        if(strHost == null || strPort == null || strApiVersion == null) {
            return;
        }
        Integer port = null;
        try{
            port = Integer.parseInt(strPort);
        } catch(NumberFormatException e) {
            ;
        }
        Integer version = null;
        try{
            version = Integer.parseInt(strApiVersion);
        } catch(NumberFormatException e) {
            ;
        }
        gmsEndpoint.setUrl(strHost.trim(), port, version);
    }

	@Override @DebugLog
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		LoggingExceptionHandler.setDefaultUncaughtExceptionHandler(log);
	}

	@DebugLog
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
        // TODO: Should delegate preference handling to Controller
		PreferenceFragment callbackFragment = (PreferenceFragment)this.getFragment(0);
        PreferenceFragment settingsFragment = (PreferenceFragment)this.getFragment(1);
		// TODO: When desired_date changes, start async task to update time_slots
		if (key.equals("scenario")) {
			checkDesiredTimeEnabled(callbackFragment);
		} else if (key.equals(Globals.PROPERTY_GMS_USER)) {
            String strGmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
            gmsRequestInterceptor.setGmsUser(strGmsUser);
        } else if (key.equals("desired_time")) {
            Preference desiredTimePref = callbackFragment.findPreference("desired_time");
            if(desiredTimePref == null) {
                return;
            }
            String desiredTime = sharedPreferences.getString("desired_time", null);
            if(desiredTime != null) {
                ListPreference selectedTimePref = (ListPreference) callbackFragment.findPreference("selected_time");
                if (selectedTimePref != null) {
                    selectedTimePref.setEnabled(false);
                    selectedTimePref.setSummary("Select desired time above");
                    selectedTimePref.setEntries(EMPTY_LIST);
                    selectedTimePref.setEntryValues(EMPTY_LIST);
                    selectedTimePref.setValue(null);
                    selectedTimePref.getEditor().remove("selected_time").apply();

                    String serviceName = sharedPreferences.getString("service_name", null);
                    controller.requestTimeSlots(serviceName, desiredTime);
                    Toast.makeText(this, "Updating time slots...", Toast.LENGTH_SHORT).show();
                }
            }
		} else if (key.equals("selected_time")) {
			ListPreference selectedTimePref = (ListPreference)callbackFragment.findPreference("selected_time");
			if(selectedTimePref == null) {
                return;
            }
            String selectedTime = sharedPreferences.getString("selected_time", null);
            if(selectedTime != null) {
				CharSequence value = selectedTimePref.getEntry();
				selectedTimePref.setSummary(value == null || value.length() == 0 ? "[nothing selected]" : value);
			}
		} else if (key.equals(Globals.PROPERTY_HOST) ||
                    key.equals(Globals.PROPERTY_PORT) ||
                    key.equals(Globals.PROPERTY_API_VERSION)) {
            tryUpdateEndpoint();
        } else if (key.equals("push_notifications_enabled")) {
            Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
            Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
            if(pushToggle != null && senderIdField != null) {
                pushToggle.setEnabled(false);
                senderIdField.setEnabled(false);
                boolean bPushEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false);
                if (bPushEnabled == true) {
                    String strGcmSenderId = sharedPreferences.getString("new_gcm_sender_id", null);
                    bus.post(new GcmRegisterEvent(strGcmSenderId));
                } else {
                    bus.post(new GcmUnregisterEvent(null));
                }
            }
        } else if (key.equals("new_gcm_sender_id")) {
            Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
            Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
            if(pushToggle != null && senderIdField != null) {
                boolean bPushEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false);
                if (!bPushEnabled) {
                    return;
                }
                pushToggle.setEnabled(false);
                senderIdField.setEnabled(false);
                String strGcmSenderId = sharedPreferences.getString("new_gcm_sender_id", null);
                bus.post(new GcmRegisterEvent(strGcmSenderId));
            }
        }
	}
	
	@Override @DebugLog
	public void onResume() {
	    super.onResume();
        bus.registerSticky(this);
	    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // NTS: Can't checkDesiredTimeEnabled here because Preference init occurs later
	}

    @Override @DebugLog
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        bus.unregister(this);
        super.onPause();
    }

    // Hack to deal with convoluted lifecycles (until this is reorganized)
    @DebugLog
    public void onFragmentResume(PreferenceFragment fragment) {
        checkDesiredTimeEnabled(fragment);
    }
    @DebugLog
    public void onFragmentPause(PreferenceFragment fragment) {
        // Nothing to do here
    }

    @DebugLog
	protected void checkDesiredTimeEnabled(PreferenceFragment callbackFragment)
	{
		Preference desiredTimePref = callbackFragment.findPreference("desired_time");
		ListPreference selectedTimePref = (ListPreference)callbackFragment.findPreference("selected_time");
		if(desiredTimePref==null || selectedTimePref == null)
		{
            log.warn("Desired Time fields not yet available.");
			// Has not yet been created. Avoid crash.
			return;
		}
		if("VOICE-SCHEDULED-USERTERM".equals(sharedPreferences.getString("scenario", null)))
		{
			desiredTimePref.setEnabled(true);
			selectedTimePref.setEnabled(true);
		}
		else {
            desiredTimePref.setEnabled(false);
            desiredTimePref.setSummary("Tap to select");
            selectedTimePref.setEnabled(false);
            selectedTimePref.setSummary("Select desired time above");
            selectedTimePref.setEntries(EMPTY_LIST);
            selectedTimePref.setEntryValues(EMPTY_LIST);
            selectedTimePref.setValue(null);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("desired_time");
            editor.remove("selected_time");
            editor.apply();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        boolean result;
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.sample_general_actions, menu);
        result = super.onCreateOptionsMenu(menu);
        this.menu = menu;
	    return result;
	}

    @Override
    public CharSequence getTabTitle(int which) {
        switch(which) {
            case 0:
                return "Connect";
            case 1:
                return "Settings";
            case 2:
                return "Queue";
            default:
                break;
        }
        return null;
    }

    @Override
    public Fragment createFragment(int which) {
        switch(which) {
            case 0:
                PreferenceWithSummaryFragment callbackFragment = PreferenceWithSummaryFragment.create(R.xml.preferences_callback);
                callbackFragment.getExcludedPreferences().add("selected_time");
                callbackFragment.getExcludedPreferences().add("desired_time");
                return callbackFragment;
            case 1:
                return PreferenceWithSummaryFragment.create(R.xml.preferences_settings);
            case 2:
                return PreferenceWithSummaryFragment.create(R.xml.preferences_queue);
            default:
                break;
        }
        return null;
    }

    @Override
    public int getTabCount() {
        return 3;
    }

    @Override
    public Integer getFragmentMenu(int which) {
        switch(which) {
            case 0:
                return R.menu.callback_actions;
            case 1:
                return null;
            case 2:
                return R.menu.queue_actions;
            default:
                break;
        }
        return null;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.connect) {
            if(!gmsEndpoint.isUrlSet()) {
                Toast.makeText(this, "Server settings not configured!", Toast.LENGTH_SHORT).show();
                log.debug("GMS Endpoint is not initialized.");
            } else {
                boolean bPushEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false);
                String strGcmRegId = sharedPreferences.getString(GcmManager.PROPERTY_REG_ID, null);
                if(bPushEnabled && strGcmRegId == null) {
                    Toast.makeText(this, "GCM is not registered!", Toast.LENGTH_SHORT).show();
                    log.debug("Push notifications required but GCM is not registered.");
                } else {
                    Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                    log.debug("Preparing Callback Start Service request...");
                    controller.connect();
                    item.setEnabled(false);
                }
            }
			return true;
		} else if(item.getItemId()==R.id.log) {
			startActivity(new Intent(this, LogActivity.class));
			return true;
		} else if(item.getItemId()==R.id.refresh_queue) {
            if(!gmsEndpoint.isUrlSet()) {
                Toast.makeText(this, "Server settings not configured!", Toast.LENGTH_SHORT).show();
                log.debug("GMS Endpoint is not initialized.");
            } else {
                // TODO: Provide session management, tracking.
                controller.checkQueuePosition();
                item.setEnabled(false);
            }
			return true;
		} else if(item.getItemId()==R.id.about) {
			DialogFragment dialog = new AboutDialogFragment();
			dialog.show(getFragmentManager(), "dialog_about");
			return true;
	    }
        return true;
	}

    /** EVENT HANDLERS ARE HERE **/

    @DebugLog
    public void onEventMainThread(GcmRegisterDoneEvent event) {
        // Because the PreferenceFragment is being used, simple things like
        // toggling Enabled is ugly.
        Toast.makeText(this, "GCM Registered!", Toast.LENGTH_SHORT).show();
        PreferenceFragment settingsFragment = (PreferenceFragment)this.getFragment(1);
        Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
        Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
        if(pushToggle != null && senderIdField != null) {
            pushToggle.setEnabled(true);
            senderIdField.setEnabled(true);
        }
    }

    @DebugLog
    public void onEventMainThread(GcmUnregisterDoneEvent event) {
        if(event.isPendingWork()) {
            // Can use EventBus priorities to cancel this in GcmManager
            return;
        }
        Toast.makeText(this, "GCM Unregistered!", Toast.LENGTH_SHORT).show();
        PreferenceFragment settingsFragment = (PreferenceFragment)this.getFragment(1);
        Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
        Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
        if(pushToggle != null && senderIdField != null) {
            pushToggle.setEnabled(true);
            senderIdField.setEnabled(true);
        }
    }

    @DebugLog
    public void onEventMainThread(GcmErrorEvent event) {
        Toast.makeText(this, "GCM Error!", Toast.LENGTH_SHORT).show();
        log.debug("GCM Error encountered: " + event.error);
        PreferenceFragment settingsFragment = (PreferenceFragment)this.getFragment(1);
        Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
        Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
        if(pushToggle != null && senderIdField != null) {
            pushToggle.setEnabled(true);
            // I'd be able to set this back to "Disabled" if it weren't a SwitchPreference...
            senderIdField.setEnabled(true);
        }
    }

    @DebugLog
    public void onEventMainThread(CallbackStartDoneEvent event) {
        MenuItem item = menu.findItem(R.id.connect);
        if(item != null) {
            item.setEnabled(true);
        }
        controller.handleDialog(event.callbackDialog);
    }

    public void onEventMainThread(CallbackAvailabilityDoneEvent event) {
        controller.updateTimeSlots(event.availability);
    }

    @DebugLog
    public void onEventMainThread(CallbackDialogDoneEvent event) {
        if(event.success) {
            controller.handleDialog(event.callbackDialog);
        }
        // TODO: Otherwise, show error
        Toast.makeText(this, "Unknown dialog error, check logs.", Toast.LENGTH_SHORT).show();
    }

    @DebugLog
    public void onEventMainThread(CallbackCheckQueueDoneEvent event) {
        MenuItem item = menu.findItem(R.id.refresh_queue);
        if(item != null) {
            item.setEnabled(true);
        }
        if(event.success) {
            controller.updateQueuePosition(event.callbackQueuePosition);
        }
        // TODO: Otherwise, show error
    }

    public void onEventMainThread(CallbackErrorEvent event) {
        log.warn(event.toString());
        MenuItem item = menu.findItem(R.id.connect);
        if(item != null) {
            item.setEnabled(true);
        }
        String exceptionMessage = event.callbackException.getMessage();
        if(exceptionMessage == null || exceptionMessage.isEmpty()) {
            // TODO:
            return;
        }
        Toast.makeText(this, exceptionMessage, Toast.LENGTH_SHORT).show();
    }

    @DebugLog
    public void onEventMainThread(GcmReceiveEvent event) {
        // Processed, remove sticky!
        bus.removeStickyEvent(event);

        if(!gmsEndpoint.isUrlSet()) {
            Toast.makeText(this, "Server settings not configured. Can't process event!", Toast.LENGTH_SHORT).show();
            log.debug("Event dropped: " + event);
            return;
        }
        String gcmMessage = event.extras.getString("message");
        if(gcmMessage == null || gcmMessage.isEmpty()) {
            // TODO: There is no message.
            return;
        }
        GcmSyncMessage gcmSyncMessage = null;
        try {
            gcmSyncMessage = gson.fromJson(gcmMessage, GcmSyncMessage.class);
        } catch (JsonSyntaxException e) {
            // TODO: Failed to parse cloud message, can't interpret
        }
        if(gcmSyncMessage != null) {
            controller.handleGcmMessage(gcmSyncMessage);
        }
    }

    public void onEventMainThread(UnknownErrorEvent event) {
        log.error(event.toString());
        MenuItem item = menu.findItem(R.id.connect);
        if(item != null) {
            item.setEnabled(true);
        }
    }

    /** EVENT HANDLERS STOP HERE **/
}
