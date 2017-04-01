package se.winterei.rtraffic.libs.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Utility;

/**
 * Created by reise on 2/25/2017.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key)
    {
        updatePreference(findPreference(key), key);
    }

    private void updatePreference(Preference preference, String key)
    {
        if (preference == null) return;
        if (preference instanceof ListPreference)
        {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
            return;
        }
        SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();

        if(preference instanceof CheckBoxPreference)
        {
            preference.setSummary(String.valueOf(sharedPrefs.getBoolean(key, true)));

            String currentKey = preference.getKey();

            if (! sharedPrefs.getBoolean(currentKey, true))
            {
                switch (currentKey)
                {
                    case "pref_notifications":
                    case "pref_background_service":
                        Utility.cancelAlarm();
                        break;
                }
            }
            else
                Utility.scheduleAlarm(new se.winterei.rtraffic.libs.settings.Preference(RTraffic.getAppContext()));
        }
        else
            preference.setSummary(sharedPrefs.getString(key, "Default"));
    }

    @Override
    public void onDestroy ()
    {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }


}
