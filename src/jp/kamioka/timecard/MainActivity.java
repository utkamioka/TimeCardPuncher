package jp.kamioka.timecard;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Format FORMAT_TIME = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button punchButton = (Button)findViewById(R.id.button_punch);
        punchButton.setOnClickListener(this);

        Log.i(TAG, "NFC Enabled="+isNfcEnabled());
    }

    @Override
    public void onClick(View v) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String calendar = preference.getString("pref_selectcalendar", null);
        Log.d(TAG, "onClick(): preference: calendar="+calendar);	
        if ( calendar == null ) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.label_notice)
            .setMessage(R.string.msg_empty_calendarname)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.label_ok, null)
            .show();
            return;
        }

        String title = preference.getString("pref_eventtitle", null);
        Log.d(TAG, "onClick(): preference: title="+title);
        if ( title == null ) {
            title = (String)getText(R.string.defaultvalue_eventtitle);
        }

        try {
            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            CalendarAccessor.Event event = new CalendarAccessor.Event(title, startTime, endTime);
            new CalendarAccessor(this).addEvent(calendar, event);

            String msg = FORMAT_TIME.format(new Date(startTime))+" "+event.title();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        } catch ( CalendarAccessException e ) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(new long[]{0,200,100,200}, -1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, R.string.label_preference).setIcon(android.R.drawable.ic_menu_manage);
        menu.add(Menu.NONE, 1, 1, R.string.label_about).setIcon(android.R.drawable.ic_menu_info_details);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
        case 0:
            Intent intent = new Intent(this, PrefActivity.class);
            startActivity(intent);
            break;
        case 1:
            new AlertDialog.Builder(this)
            .setTitle(R.string.label_version)
            .setMessage(Const.NAME+"-"+Const.VERSION)
            .setPositiveButton("OK", null)
            .show();
            break;
        }
        return true;
    }

    public boolean isNfcEnabled() {
        try {
            return NfcAdapter.getDefaultAdapter().isEnabled();
        } catch ( Exception e ) {
            return false;
        }
    }
}