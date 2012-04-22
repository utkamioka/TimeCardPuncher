package jp.kamioka.timecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) Log.v(TAG, "onCreate(): in: bundle="+savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button punchButton = (Button)findViewById(R.id.button_punch);
        punchButton.setOnClickListener(this);
        Typeface face = Typeface.createFromAsset(getAssets(), "Ricty-Bold.ttf");
        punchButton.setTypeface(face);

        Log.i(TAG, "NFC Enabled="+isNfcEnabled());
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

        new CalendarEventTask(this).execute(calendar, title);
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

    private boolean isNfcEnabled() {
        try {
            return NfcAdapter.getDefaultAdapter().isEnabled();
        } catch ( Exception e ) {
            return false;
        }
    }
}