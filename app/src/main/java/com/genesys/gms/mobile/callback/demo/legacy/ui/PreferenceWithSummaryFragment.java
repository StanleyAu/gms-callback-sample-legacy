package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.*;
import android.support.v4.preference.PreferenceFragment;
import android.text.InputType;
import hugo.weaving.DebugLog;

import javax.inject.Inject;

// TODO: A multi-purpose generic fragment is actually more trouble than it's worth
public class PreferenceWithSummaryFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;
	protected Set<String> excludedPreferences = new HashSet<String>();
	
	public static PreferenceWithSummaryFragment create(int preferencesResId) {
		PreferenceWithSummaryFragment f = new PreferenceWithSummaryFragment();
		Bundle args = new Bundle();
		args.putInt("preferences", preferencesResId);
		f.setArguments(args);
		return f;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("exclusions", excludedPreferences.toArray(new String[excludedPreferences.size()]));
        ListPreference selectedTimePref = (ListPreference)findPreference("selected_time");
        if(selectedTimePref != null) {
            outState.putCharSequenceArray("timeKeys", selectedTimePref.getEntries());
            outState.putCharSequenceArray("timeValues", selectedTimePref.getEntryValues());
            outState.putCharSequence("selectedSummary", selectedTimePref.getSummary());
        }

        Preference desiredTimePref = findPreference("desired_time");
        if(desiredTimePref != null) {
            outState.putCharSequence("desiredSummary", desiredTimePref.getSummary());
        }
    }

    @Override
    public void onViewStateRestored(Bundle inState) {
        super.onViewStateRestored(inState);

        Preference desiredTimePref = findPreference("desired_time");
        if(desiredTimePref != null && inState != null) {
            CharSequence desiredSummary = inState.getCharSequence("desiredSummary");
            if(desiredSummary != null) {
                desiredTimePref.setSummary(desiredSummary);
            }
        }

        ListPreference selectedTimePref = (ListPreference)findPreference("selected_time");
        if(selectedTimePref != null && inState != null) {
            String[] exclusions = inState.getStringArray("exclusions");
            if (exclusions != null) {
                excludedPreferences = new HashSet<String>(Arrays.asList(inState.getStringArray("exclusions")));
            }
            CharSequence[] timeKeys = inState.getCharSequenceArray("timeKeys");
            CharSequence[] timeValues = inState.getCharSequenceArray("timeValues");
            if (timeKeys == null || timeValues == null || timeKeys.length == 0) {
                selectedTimePref.setSummary("Select desired time above");
                return;
            }
            selectedTimePref.setEntries(timeKeys);
            selectedTimePref.setEntryValues(timeValues);
            CharSequence selectedSummary = inState.getCharSequence("selectedSummary");
            if(selectedSummary != null) {
                selectedTimePref.setSummary(selectedSummary);
            }
        }
    }

    @Override @DebugLog
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        sharedPreferences = getPreferenceManager().getSharedPreferences();
	    addPreferencesFromResource(getArguments().getInt("preferences"));
	}

	@Override @DebugLog
	public void onResume() {
	    super.onResume();
	    updatePreferenceSummary(getPreferenceScreen());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // This is bad form but inevitable because we use this generic fragment
        ((GenesysSampleActivity)getActivity()).onFragmentResume(this);
	}

	@Override @DebugLog
	public void onPause() {
        // This is bad form but inevitable because we use this generic fragment
        ((GenesysSampleActivity)getActivity()).onFragmentPause(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	    super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = getPreferenceScreen().findPreference(key);
		updatePreferenceSummary(pref);
	}
	
	public Set<String> getExcludedPreferences()
	{
		return excludedPreferences;
	}

	protected void updatePreferenceSummary(Preference pref) {
		if (pref == null || pref instanceof TwoStatePreference || excludedPreferences.contains(pref.getKey())) {
			return;
		}
		else if (pref instanceof PreferenceGroup) {
			PreferenceGroup prefGroup = (PreferenceGroup)pref;
		    for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
		    	updatePreferenceSummary(prefGroup.getPreference(i));
		    }
		}
		else if (pref instanceof ListPreference) {
			CharSequence value = ((ListPreference)pref).getEntry();
			pref.setSummary(value == null || value.length() == 0 ? "[nothing selected]" : value);
		}
		else if (pref instanceof EditTextPreference) {
			EditTextPreference textPref = (EditTextPreference)pref;
			boolean isPassword = (textPref.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0;
			if (!isPassword) {
				String text = textPref.getText();
				pref.setSummary(text == null || text.isEmpty() ? "[empty]" : text);
			}
		}
		else {
			Object value = sharedPreferences.getAll().get(pref.getKey());
			String text = value == null ||
						  (value instanceof String && ((String) value).isEmpty()) ?
								"[empty]" :
								value.toString();
			pref.setSummary(text);
		}
	}
	
}