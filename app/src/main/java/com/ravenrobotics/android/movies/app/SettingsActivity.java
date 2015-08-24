package com.ravenrobotics.android.movies.app;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // Attach an OnPreferenceChangeListener so the UI summary can be updated when the
        // preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.prefs_sort_order_key)));
    } // end method onCreate

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Sets the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Triggers the listener immediately with the preference's current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    } // end method bindPreferenceSummaryToValue

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, looks up the correct display value in the preference's
            // 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, sets the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    } // end method onPreferenceChange

} // end class SettingsActivity