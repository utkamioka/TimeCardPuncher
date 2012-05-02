package jp.kamioka.timecard;

import jp.kamioka.timecard.event.CalendarEntryEvent;
import jp.kamioka.timecard.event.CalendarEntryListener;
import jp.kamioka.timecard.event.TimeAdjusterEvent;
import jp.kamioka.timecard.event.TimeAdjusterListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener, CalendarEntryListener, View.OnLongClickListener, AdapterView.OnItemLongClickListener, TimeAdjusterListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = true;

    private Button mPunchButton = null;
    private ListView mHistoryView = null;
    private CalendarEntryAdapter mCalendarEntryAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (LOCAL_LOGV) Log.v(TAG, "onCreate(): in: bundle="+savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPunchButton = (Button)findViewById(R.id.button_punch);
        mPunchButton.setOnClickListener(this);
        mPunchButton.setOnLongClickListener(this);

        mHistoryView = (ListView)findViewById(R.id.list_history);
        mCalendarEntryAdapter = new CalendarEntryAdapter(this);
        mHistoryView.setAdapter(mCalendarEntryAdapter);
        mHistoryView.setOnItemLongClickListener(this);

        Log.i(TAG, "NFC Enabled="+isNfcEnabled());
    }

    @Override
    public void onClick(View v) {
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): in: view="+v);
        if (v==mPunchButton) {
            _punchTimeCard();
        }
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
            .setTitle(R.string.version)
            .setMessage(Const.NAME+"-"+Const.VERSION)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(android.R.string.ok, null)
            .show();
            break;
        }
        return true;
    }

    @Override
    public void calendarEntryAdded(CalendarEntryEvent event) {
        if (LOCAL_LOGV) Log.v(TAG, "calendarEntryAdded(): event="+event);
        mCalendarEntryAdapter.addCalendarEntry(event.getCalendarEntry());
        mHistoryView.setSelectionFromTop(0, 0);
    }

    @Override
    public void calendarEntryRemoved(CalendarEntryEvent event) {
        if (LOCAL_LOGV) Log.v(TAG, "calendarEntryRemoved(): event="+event);
        mCalendarEntryAdapter.removeCalendarEntry(event.getCalendarEntry());
    }

    private boolean isNfcEnabled() {
        try {
            return NfcAdapter.getDefaultAdapter().isEnabled();
        } catch ( Exception e ) {
            return false;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent==mHistoryView) {
            final CalendarEntry calendarEntry = (CalendarEntry)mCalendarEntryAdapter.getItem(position);
            new AlertDialog.Builder(this)
            .setTitle(R.string.confirm)
            .setMessage(R.string.msg_ask_delete)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _removeCalendarEntry(calendarEntry);
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
            return true;
        }
        return false;
    }

    /**
     * 打刻する。
     */
    private void _punchTimeCard() {
        _punchTimeCard(System.currentTimeMillis());
    }

    private void _punchTimeCard(long time) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);

        // カレンダー名
        String calendar = preference.getString("pref_selectcalendar", null);
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): preference: calendar="+calendar);    
        if (calendar == null) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.notice)
            .setMessage(R.string.msg_empty_calendarname)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(android.R.string.ok, null)
            .show();
            return;
        }

        // タイトル
        String title = preference.getString("pref_eventtitle", null);
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): preference: title="+title);
        if ( title == null ) {
            title = (String)getText(R.string.defaultvalue_eventtitle);
        }
        
        CalendarEntry calendarEntry = new CalendarEntry(calendar, time, title);
        calendarEntry.setWriteFlag(true);
        _addCalendarEntry(calendarEntry);
    }

    /**
     * カレンダーにイベントを追加する。
     * @param calendarEntry
     */
    private void _addCalendarEntry(CalendarEntry calendarEntry) {
        CalendarEventTask task = new CalendarEventTask(this);
        task.addCalendarListener(this);
        task.execute(new CalendarEventTask.CalendarRequestAdd(calendarEntry));
    }

    /**
     * カレンダーからイベントを削除する。
     * @param calendarEntry
     */
    private void _removeCalendarEntry(CalendarEntry calendarEntry) {
        CalendarEventTask task = new CalendarEventTask(this);
        task.addCalendarListener(this);
        task.execute(new CalendarEventTask.CalendarRequestRemove(calendarEntry));
    }

    @Override
    public boolean onLongClick(View view) {
        if (view==mPunchButton) {
            _punchTimeCardWithTimeAdjust();
            return true;
        }
        return false;
    }

    private void _punchTimeCardWithTimeAdjust() {
        new TimeAdjustDialog(this, System.currentTimeMillis())
        .setAdjustButton(R.string.label_punch, this)
        .show();
    }

    @Override
    public void onTimeAdjusted(TimeAdjusterEvent event) {
        long time = event.getTimeAndOffset()[0];
        _punchTimeCard(time);
    }
}