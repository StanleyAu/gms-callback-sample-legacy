package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.TwoStatePreference;
import android.text.InputType;

public class PreferenceWithSummaryFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	protected Set<String> excludedPreferences = new HashSet<String>();
	
	public static PreferenceWithSummaryFragment create(int preferencesResId) {
		PreferenceWithSummaryFragment f = new PreferenceWithSummaryFragment();
		Bundle args = new Bundle();
		args.putInt("preferences", preferencesResId);
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(getArguments().getInt("preferences"));
	}

	@Override
	public void onResume() {
	    super.onResume();
	    updatePreferenceSummary(getPreferenceScreen());	    
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
			// do nothing
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
			Object value = getPreferenceManager().getSharedPreferences().getAll().get(pref.getKey());
			String text = value == null ||
						  (value instanceof String && ((String) value).isEmpty()) ?
								"[empty]" :
								value.toString();
			pref.setSummary(text);
		}
	}
	
}