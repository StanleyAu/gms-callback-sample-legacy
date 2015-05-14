package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.DialogFragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.capture.CaptureManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.GcmManager;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.GcmSyncMessage;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.UnknownErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.capture.StartCaptureEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.gcm.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsRequestInterceptor;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import timber.log.Timber;

import javax.inject.Inject;

public class GenesysSampleActivity extends AbstractTabActivity implements OnSharedPreferenceChangeListener {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CaptureManager.CREATE_SCREEN_CAPTURE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        bus.post(new StartCaptureEvent(resultCode, data));
    }

    private void tryUpdateEndpoint() {
        String strHost = sharedPreferences.getString(Globals.PROPERTY_HOST, null);
        String strPort = sharedPreferences.getString(Globals.PROPERTY_PORT, null);
        String strApiVersion = sharedPreferences.getString(Globals.PROPERTY_API_VERSION, null);
        if(strHost == null || strPort == null || strApiVersion == null) {
            return;
        }
        Timber.d("strPort: %s, strApiVersion: %s", strPort, strApiVersion);
        Integer port = null;
        Integer version = null;
        try {
            port = Integer.valueOf(strPort);
            version = Integer.valueOf(strApiVersion);
        } catch(NumberFormatException e) {
            ;
        }
        gmsEndpoint.setUrl(strHost.trim(), port, version);
    }

	@Override
	public void onCreate(Bundle inState) {
	    super.onCreate(inState);
		//LoggingExceptionHandler.setDefaultUncaughtExceptionHandler(log);

        if(inState != null) {
            controller.restoreState(inState);
        }
	}

    // TODO: This really needs to be broken up into individual handlers
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
                if (bPushEnabled) {
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
	
	@Override
	public void onResume() {
	    super.onResume();
        bus.registerSticky(this);
	    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // NTS: Can't checkDesiredTimeEnabled here because Preference init occurs later
	}

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        controller.persistState(outState);
        super.onSaveInstanceState(outState);
    }

    // Hack to deal with convoluted lifecycles (until this is reorganized)
    public void onFragmentResume(PreferenceFragment fragment) {
        checkDesiredTimeEnabled(fragment);
    }
    public void onFragmentPause(PreferenceFragment fragment) {
        // Nothing to do here
    }

	protected void checkDesiredTimeEnabled(PreferenceFragment callbackFragment)
	{
		Preference desiredTimePref = callbackFragment.findPreference("desired_time");
		ListPreference selectedTimePref = (ListPreference)callbackFragment.findPreference("selected_time");
		if(desiredTimePref==null || selectedTimePref == null) {
			// Has not yet been created. Avoid crash.
			return;
		}
		if("VOICE-SCHEDULED-USERTERM".equals(sharedPreferences.getString("scenario", null))) {
			desiredTimePref.setEnabled(true);
			selectedTimePref.setEnabled(true);
		} else {
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
                Timber.w("GmsEndpoint has not been initialized.");
            } else {
                boolean bPushEnabled = sharedPreferences.getBoolean("push_notifications_enabled", false);
                String strGcmRegId = sharedPreferences.getString(GcmManager.PROPERTY_REG_ID, null);
                if(bPushEnabled && strGcmRegId == null) {
                    Toast.makeText(this, "GCM is not registered!", Toast.LENGTH_SHORT).show();
                    Timber.w("Push Notifications enabled but GCM is not registered.");
                } else {
                    Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
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
                // Repeated code
                Toast.makeText(this, "Server settings not configured!", Toast.LENGTH_SHORT).show();
                Timber.w("GmsEndpoint has not been initialized.");
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
	    } else if(item.getItemId()==R.id.test) {
            /*
            String sampleData = "{\"channel\":\"/_genesys\",\"data\":{\"message\":{\"transcriptPosition\":\"2\",\"startedAt\":\"2015-02-10T22:39:23Z\",\"chatServiceMessage\":\"Chat service is available\",\"transcriptToShow\":[[\"Notice.Joined\",\"chat1\",\"has joined the session\",\"6\",\"AGENT\"]],\"chatSessionId\":\"0000YaADMWXM0013\",\"chatIxnState\":\"TRANSCRIPT\"},\"id\":\"ad41d420b17511e4adf163edd6718665\",\"tag\":\"service.chat.refresh.180-92e0f9e6-8096-4ccb-94f4-c0d03e14fd8d\"}}";
            ChatCometResponse chatCometResponse = gson.fromJson(sampleData, ChatCometResponse.class);
            Log.d("TEST_VALUE", chatCometResponse.toString());

            String dialogData = "{\"error\":\"\",\"_dialogId\":\"\",\"_id\":\"\",\"_action\":\"DialNumber\",\"_tel_url\":\"\",\"_label\":\"\",\"_content\":[{\"_group_name\":\"groupie\",\"_group_content\":[{\"_label\":\"groupLabel1\",\"_user_action_url\":\"url1\"}]}],\"_chat_parameters\":{\"subject\":\"subject\"}}";
            CallbackDialog callbackDialog = gson.fromJson(dialogData, CallbackDialog.class);
            Log.d("TEST_VALUE", callbackDialog.toString());
            */
            Timber.tag("TEST_VALUE");
            Timber.d("Launching GenesysChatActivity");
            Intent intent = new Intent(this, GenesysChatActivity.class);
            intent.setAction(Globals.ACTION_GENESYS_START_CHAT);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            return true;
        } else if(item.getItemId()==R.id.share_screen) {
            CaptureManager.fireScreenCaptureEvent(this);
        }
        return true;
	}

    /** EVENT HANDLERS ARE HERE **/

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

    public void onEventMainThread(GcmErrorEvent event) {
        Toast.makeText(this, "GCM Error!", Toast.LENGTH_SHORT).show();
        Timber.e(event.error, "GCM Error encountered.");
        PreferenceFragment settingsFragment = (PreferenceFragment)this.getFragment(1);
        Preference pushToggle = settingsFragment.findPreference("push_notifications_enabled");
        Preference senderIdField = settingsFragment.findPreference("new_gcm_sender_id");
        if(pushToggle != null && senderIdField != null) {
            pushToggle.setEnabled(true);
            // I'd be able to set this back to "Disabled" if it weren't a SwitchPreference...
            senderIdField.setEnabled(true);
        }
    }

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

    public void onEventMainThread(CallbackDialogDoneEvent event) {
        if(event.success) {
            controller.handleDialog(event.callbackDialog);
        } else {
            // TODO: Otherwise, show error
            Toast.makeText(this, "Unknown dialog error, check logs.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onEventMainThread(CallbackCheckQueueDoneEvent event) {
        MenuItem item = menu.findItem(R.id.refresh_queue);
        if(item != null) {
            item.setEnabled(true);
        }
        if(event.success) {
            controller.updateQueuePosition(event.callbackQueuePosition);
        } else {
            // TODO: Otherwise, show error
            Toast.makeText(this, "Unable to check queue!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onEventMainThread(CallbackErrorEvent event) {
        Timber.e("CallbackErrorEvent received: %s", event.callbackException);
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

    public void onEventMainThread(GcmReceiveEvent event) {
        // Processed, remove sticky!
        bus.removeStickyEvent(event);

        if(!gmsEndpoint.isUrlSet()) {
            Toast.makeText(this, "Server settings not configured. Can't process event!", Toast.LENGTH_SHORT).show();
            Timber.d("GcmReceiveEvent dropped: Server settings not configured.");
            return;
        }
        String gcmMessage = event.extras.getString("message");
        if(gcmMessage == null || gcmMessage.isEmpty()) {
            // TODO: There is no message.
            Timber.d("GcmReceiveEvent dropped: No message found.");
            return;
        }
        GcmSyncMessage gcmSyncMessage = null;
        try {
            gcmSyncMessage = gson.fromJson(gcmMessage, GcmSyncMessage.class);
        } catch (JsonSyntaxException e) {
            // TODO: Failed to parse cloud message, can't interpret
            Timber.w("Unable to parse message into GcmSyncMessage: %s", gcmMessage);
        }
        if(gcmSyncMessage != null && gcmSyncMessage.getAction() != null) {
            controller.handleGcmMessage(gcmSyncMessage);
        } else {
            // TODO: Handle a Chat notification in foreground
        }
    }

    public void onEventMainThread(UnknownErrorEvent event) {
        Timber.e(event.error, "UnknownErrorEvent received.");
        MenuItem item = menu.findItem(R.id.connect);
        if(item != null) {
            item.setEnabled(true);
        }
        Toast.makeText(this, event.error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /** EVENT HANDLERS STOP HERE **/
}
