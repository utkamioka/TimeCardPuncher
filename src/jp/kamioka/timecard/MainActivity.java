package jp.kamioka.timecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;

public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = false;

    private static final Format FORMAT_TIME = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) Log.v(TAG, "onCreate(): in: bundle="+savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button punchButton = (Button)findViewById(R.id.button_punch);
        punchButton.setOnClickListener(this);

        Log.i(TAG, "NFC Enabled="+isNfcEnabled());

        Log.v(TAG, ""+Settings.System.DEFAULT_NOTIFICATION_URI);
    }

    @Override
    public void onClick(View v) {
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): in: view="+v);
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String calendar = preference.getString("pref_selectcalendar", null);
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): preference: calendar="+calendar);	
        if (calendar == null) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.label_notice)
            .setMessage(R.string.msg_empty_calendarname)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.label_ok, null)
            .show();
            return;
        }

        String title = preference.getString("pref_eventtitle", null);
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): preference: title="+title);
        if ( title == null ) {
            title = (String)getText(R.string.defaultvalue_eventtitle);
        }

        try {
            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            CalendarAccessor.Event event = new CalendarAccessor.Event(title, startTime, endTime);
            boolean writeEventFlag = true;
            new CalendarAccessor(this).addEvent(calendar, event, writeEventFlag);

            String msg = FORMAT_TIME.format(new Date(startTime))+" "+event.title();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            notify(null, DEFAULT_NOTIFICATION_URI, new long[]{0,200});
        } catch ( CalendarAccessException e ) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            notify(null, null, new long[]{0,200,100,200});
        }
    }

    private void notify(String ticker, Uri sound, long[] vibrate)
    {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifyWithSound = preference.getBoolean("pref_notify_with_sound", false);
        boolean notifyWithVibrate = preference.getBoolean("pref_notify_with_vibrate", false);

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        if (ticker != null) notification.tickerText = ticker;
        if (sound != null && notifyWithSound) notification.sound = sound;
        if (vibrate != null && notifyWithVibrate) notification.vibrate = vibrate;
        NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(1, notification);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (LOCAL_LOGV) Log.v(TAG, "onCreateOptionsMenu(): in: menu="+menu);
        menu.add(Menu.NONE, 0, 0, R.string.label_preference).setIcon(android.R.drawable.ic_menu_manage);
        menu.add(Menu.NONE, 1, 1, R.string.label_about).setIcon(android.R.drawable.ic_menu_info_details);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (LOCAL_LOGV) Log.v(TAG, "onOptionsItemSelected(): in: item="+item);
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