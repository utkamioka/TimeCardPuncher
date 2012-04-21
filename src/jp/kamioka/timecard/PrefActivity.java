package jp.kamioka.timecard;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputType;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PrefActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    private static final String TAG = PrefActivity.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) Log.v(TAG, "onCreate(): in: bundle="+savedInstanceState);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        ListPreference selectcalendar = (ListPreference)getPreferenceScreen().findPreference("pref_selectcalendar");
        selectcalendar.setOnPreferenceChangeListener(this);

        EditTextPreference eventtitle = (EditTextPreference)getPreferenceScreen().findPreference("pref_eventtitle");
        // Disable multi-line input
        eventtitle.getEditText().setInputType(InputType.TYPE_CLASS_TEXT);
        eventtitle.setSummary(eventtitle.getText());
        eventtitle.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        if (LOCAL_LOGV) Log.v(TAG, "onResume(): in:");
        super.onResume();

        String[] calendars = new CalendarAccessor(this).getCalendars();
        if (LOCAL_LOGV) Log.v(TAG, "onResume(): calendars="+Arrays.asList(calendars));

        List<String> list = new ArrayList<String>(Arrays.asList(calendars));
        list.add("Dummy entry");
        list.add(""+new Date());
        calendars = (String[])list.toArray(new String[0]);

        ListPreference selectcalendar = (ListPreference)getPreferenceScreen().findPreference("pref_selectcalendar");
        selectcalendar.setEntries((CharSequence[])calendars);
        selectcalendar.setEntryValues((CharSequence[])calendars);
        if (LOCAL_LOGV) Log.v(TAG, "onResume(): entry="+selectcalendar.getEntry());
        selectcalendar.setSummary(selectcalendar.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (LOCAL_LOGV) Log.v(TAG, "onPreferenceChange(): in: key="+preference.getKey()+", value="+newValue);
        if(preference.getKey().equals("pref_selectcalendar")){
            preference.setSummary(""+newValue);
            Log.i(TAG, "onPreferenceChanged(): out: "+preference.getKey()+"="+newValue);
            return true;
        }
        if(preference.getKey().equals("pref_eventtitle")){
            preference.setSummary(""+newValue);
            Log.i(TAG, "onPreferenceChanged(): out: "+preference.getKey()+"="+newValue);
            return true;
        }
        return false;
    }
}
