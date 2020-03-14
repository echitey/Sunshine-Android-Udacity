package br.com.echitey.android.sunshineapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_forecast);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        for(int i=0; i<count; i++){
            Preference p = preferenceScreen.getPreference(i);

            if(!(p instanceof CheckBoxPreference)){
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    private void setPreferenceSummary(Preference preference, String value){
        if(preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference)preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if(prefIndex >= 0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof EditTextPreference){
            preference.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(null != preference){
            if(!(preference instanceof CheckBoxPreference)) {
                String label = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, label);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
