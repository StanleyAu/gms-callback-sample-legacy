package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.DialogFragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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

// TODO: Possibly redesign how the communication from service to activity is done. Now it is done with explicit intents, configuring the activity as singleTask.
public class GenesysSampleActivity extends AbstractTabActivity implements OnSharedPreferenceChangeListener {

	private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);

	private GenesysController controller;

	@DebugLog
	public GenesysSampleActivity() {
		super(new TabElement[] {
				new TabElement(
						"Connect",
						PreferenceWithSummaryFragment.create(R.xml.preferences_callback),
						R.drawable.ic_action_cloud,
						R.menu.callback_actions),
				new TabElement(
						"Settings",
						PreferenceWithSummaryFragment.create(R.xml.preferences_settings),
						R.drawable.ic_action_settings,
						null),
				new TabElement(
						"Queue",
						PreferenceWithSummaryFragment.create(R.xml.preferences_queue),
						R.drawable.ic_action_forward,
						R.menu.queue_actions),
			});
		// Hack to prevent auto-updating of selected_time
		PreferenceWithSummaryFragment callbackFragment = (PreferenceWithSummaryFragment)tabs[0].fragment;
		callbackFragment.getExcludedPreferences().add("selected_time");
		callbackFragment.getExcludedPreferences().add("desired_time");
	}

	@Override @DebugLog
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		Globals.setupLogging(this);
		LoggingExceptionHandler.setDefaultUncaughtExceptionHandler(log);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@DebugLog
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		PreferenceFragment callbackFragment = (PreferenceFragment)tabs[0].fragment;
		// TODO: When desired_date changes, start async task to update time_slots
		if (key.equals("scenario"))
		{
			checkDesiredTimeEnabled();
		}
		else if (key.equals("desired_time"))
		{
			ListPreference selectedTime = (ListPreference)callbackFragment.findPreference("selected_time");
			if(selectedTime!=null)
			{
				String serviceName = sharedPreferences.getString("service_name", null);
				String desiredTime = sharedPreferences.getString("desired_time", null);
				controller.requestTimeSlots(serviceName, desiredTime);
				selectedTime.setEnabled(false);
				Toast.makeText(this, "Updating time slots...", Toast.LENGTH_SHORT).show();
			}
		}
		else if (key.equals("selected_time"))
		{
			ListPreference selectedTime = (ListPreference)callbackFragment.findPreference("selected_time");
			if(selectedTime!=null)
			{
				CharSequence value = selectedTime.getEntry();
				selectedTime.setSummary(value == null || value.length() == 0 ? "[nothing selected]" : value);
			}
		}
	}
	
	@Override @DebugLog
	public void onResume() {
	    super.onResume();
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override @DebugLog
	public void onWindowFocusChanged (boolean hasFocus) {
		log.debug("onWindowFocusChanged");
		checkDesiredTimeEnabled();
	}
	
	protected void checkDesiredTimeEnabled()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		PreferenceFragment callbackFragment = (PreferenceFragment)tabs[0].fragment;
		Preference desiredTime = (Preference)callbackFragment.findPreference("desired_time");
		Preference selectedTime = (Preference)callbackFragment.findPreference("selected_time");
		if(desiredTime==null || selectedTime == null)
		{
			// Has not yet been created. Avoid crash.
			return;
		}
		if("VOICE-SCHEDULED-USERTERM".equals(sharedPreferences.getString("scenario", null)))
		{
			desiredTime.setEnabled(true);
			selectedTime.setEnabled(true);
		}
		else
		{
			desiredTime.setEnabled(false);
			selectedTime.setEnabled(false);
		}
	}

	@Override
	public void onPause() {
	    super.onPause();
	    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.sample_general_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onGenesysServiceConnected(GenesysService genesysService) {
	    controller = new GenesysController(genesysService);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.connect) {
			Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
			controller.connect(this);
			return true;
		} else if(item.getItemId()==R.id.log) {
			startActivity(new Intent(this, LogActivity.class));
			return true;
		} else if(item.getItemId()==R.id.refresh_queue) {
			controller.refreshQueue();
			return true;
		} else if(item.getItemId()==R.id.about) {
			DialogFragment dialog = new AboutDialogFragment();
			dialog.show(getFragmentManager(), "dialog_about");
			return true;
		} else {
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void handleIntent(Intent intent) {
		if (controller != null)
			controller.handleIntent(this, intent);
	}

}
