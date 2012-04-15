package jp.kamioka.timecard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class PrefActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
	private static final String TAG = "PrefActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate():");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		
		ListPreference selectcalendar = (ListPreference)getPreferenceScreen().findPreference("pref_selectcalendar");
		selectcalendar.setOnPreferenceChangeListener(this);
	}
	
	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume():");
		super.onResume();

		String[] calendars = new CalendarAccessor(this).getCalendars();
		Log.d(TAG, "onResume(): calendars="+Arrays.asList(calendars));

		List<String> list = new ArrayList<String>(Arrays.asList(calendars));
		list.add("Dummy entry");
		list.add(""+new Date());
		calendars = (String[])list.toArray(new String[0]);
		
		ListPreference selectcalendar = (ListPreference)getPreferenceScreen().findPreference("pref_selectcalendar");
		selectcalendar.setEntries((CharSequence[])calendars);
		selectcalendar.setEntryValues((CharSequence[])calendars);
		Log.d(TAG, "onResume(): entry="+selectcalendar.getEntry());
		selectcalendar.setSummary(selectcalendar.getEntry());
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue)
	{
		Log.d(TAG, "onPreferenceChange(): key="+preference.getKey());
		if(preference.getKey().equals("pref_selectcalendar")){
			preference.setSummary(""+newValue);
			return true;
		}
		return false;
	}
}
