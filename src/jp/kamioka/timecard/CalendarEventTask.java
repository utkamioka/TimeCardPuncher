package jp.kamioka.timecard;

import static android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.kamioka.timecard.CalendarEventTask.CalendarRequest;
import jp.kamioka.timecard.CalendarEventTask.CalendarResult;
import jp.kamioka.timecard.event.CalendarEntryEvent;
import jp.kamioka.timecard.event.CalendarEntryListener;

public class CalendarEventTask extends AsyncTask<CalendarRequest, Void, AsyncTaskResult<CalendarResult>> {
    private static final String TAG = CalendarEventTask.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = true;

    private Activity mActivity;
    private List<CalendarEntryListener> mCalendarListeners = new ArrayList<CalendarEntryListener>();

    public CalendarEventTask(Activity activity){
        mActivity = activity;
    }

    public void addCalendarListener(CalendarEntryListener l) {
        mCalendarListeners.add(l);
    }
    public void removeCalendarListener(CalendarEntryListener l) {
        mCalendarListeners.remove(l);
    }
    private void onCalendarEntryAdded(CalendarResult result) {
        CalendarEntry entry = result.getCalendarEntry();
        CalendarEntryEvent event = new CalendarEntryEvent(this, entry);
        for (CalendarEntryListener l : mCalendarListeners) {
            l.calendarEntryAdded(event);
        }
    }
    private void onCalendarEntryRemoved(CalendarResult result) {
        CalendarEntry entry = result.getCalendarEntry();
        CalendarEntryEvent event = new CalendarEntryEvent(this, entry);
        for (CalendarEntryListener l : mCalendarListeners) {
            l.calendarEntryRemoved(event);
        }
    }

    @Override
    protected AsyncTaskResult<CalendarResult> doInBackground(CalendarRequest... args) {
        if (LOCAL_LOGV) Log.v(TAG, "doInBackground(): args="+Arrays.asList(args));
        if (args[0].isAdd()) {
            return _addCalendarEntry((CalendarRequestAdd)args[0]);
        } else if (args[0].isRemove()) {
            return _removeCalendarEntry((CalendarRequestRemove)args[0]);
        }
        return new AsyncTaskResult<CalendarResult>(false);
    }

    /**
     * カレンダーにイベントを追加する。
     * @param request
     * @return
     */
    private AsyncTaskResult<CalendarResult> _addCalendarEntry(CalendarRequestAdd request) {
        CalendarEntry calendarEntry = request.getCalendarEntry();
        String calendar = calendarEntry.getCalendar();
        long time = calendarEntry.getTime();
        String title = calendarEntry.getTitle();
        boolean writeEventFlag = calendarEntry.getWriteFlag();
        try {
            CalendarAccessor.Event event = new CalendarAccessor.Event(title, time, time);
            Uri uri = new CalendarAccessor(mActivity).addEvent(calendar, event, writeEventFlag);
            calendarEntry.setUri(uri);
            CalendarResult result = new CalendarResultAdd(calendarEntry);
            return new AsyncTaskResult<CalendarResult>(true, result);
        } catch (CalendarAccessException e) {
            return new AsyncTaskResult<CalendarResult>(e);
        }
    }

    /**
     * カレンダーからイベントを削除する。
     * @param request
     * @return
     */
    private AsyncTaskResult<CalendarResult> _removeCalendarEntry(CalendarRequestRemove request) {
        CalendarEntry calendarEntry = request.getCalendarEntry();
        Uri uri = calendarEntry.getUri();
        if (uri!=null) {
            new CalendarAccessor(mActivity).removeEvent(calendarEntry.getUri());
        }
        CalendarResult result = new CalendarResultRemove(calendarEntry);
        return new AsyncTaskResult<CalendarResult>(true, result);
    }

    @Override
    public void onPostExecute(AsyncTaskResult<CalendarResult> result) {
        if (LOCAL_LOGV) Log.v(TAG, "onPostExecute(): result="+result);
        if (result.getStatus()) {
            _notify(null, DEFAULT_NOTIFICATION_URI, new long[]{0,200});
            if (result.getContent().isAdd()) {
                onCalendarEntryAdded(result.getContent());
            } else if (result.getContent().isRemove()) {
                onCalendarEntryRemoved(result.getContent());
            }
        } else {
            String msg = "Calendar access failure.";
            if (result.getCause()!=null) {
                Log.w(TAG, msg, result.getCause());
                msg = result.getCause().getMessage();
            } else {
                Log.w(TAG, msg);
            }
            Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
            _notify(msg, null, new long[]{0,200,100,200});
        }
    }

    /**
     * 通知する。
     * @param ticker
     * @param sound
     * @param vibrate
     */
    private void _notify(String ticker, Uri sound, long[] vibrate)
    {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean notifyWithSound = preference.getBoolean("pref_notify_with_sound", false);
        boolean notifyWithVibrate = preference.getBoolean("pref_notify_with_vibrate", false);

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        if (ticker != null) notification.tickerText = ticker;
        if (sound != null && notifyWithSound) notification.sound = sound;
        if (vibrate != null && notifyWithVibrate) notification.vibrate = vibrate;
        NotificationManager nManager = (NotificationManager)mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(1, notification);
    }

    public abstract static class CalendarRequest {
        private CalendarEntry mCalendarEntry;
        public abstract boolean isAdd();
        public abstract boolean isRemove();
        public CalendarRequest(CalendarEntry entry) {
            mCalendarEntry = entry;
        }
        public CalendarEntry getCalendarEntry() { return mCalendarEntry; }
    }
    public static class CalendarRequestAdd extends CalendarRequest {
        public CalendarRequestAdd(CalendarEntry entry) {
            super(entry);
        }
        @Override
        public boolean isAdd() { return true; }
        @Override
        public boolean isRemove() { return false; }
    }
    public static class CalendarRequestRemove extends CalendarRequest {
        public CalendarRequestRemove(CalendarEntry entry) {
            super(entry);
        }
        @Override
        public boolean isAdd() { return false; }
        @Override
        public boolean isRemove() { return true; }
    }

    public abstract static class CalendarResult {
        private CalendarEntry mCalendarEntry;
        public abstract boolean isAdd();
        public abstract boolean isRemove();
        public CalendarResult(CalendarEntry entry) {
            mCalendarEntry = entry;
        }
        public CalendarEntry getCalendarEntry() { return mCalendarEntry; }
    }
    public static class CalendarResultAdd extends CalendarResult {
        public CalendarResultAdd(CalendarEntry entry) {
            super(entry);
        }
        @Override
        public boolean isAdd() { return true; }
        @Override
        public boolean isRemove() { return false; }
    }
    public static class CalendarResultRemove extends CalendarResult {
        public CalendarResultRemove(CalendarEntry entry) {
            super(entry);
        }
        @Override
        public boolean isAdd() { return false; }
        @Override
        public boolean isRemove() { return true; }
    }
}