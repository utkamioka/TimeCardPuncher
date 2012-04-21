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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CalendarEventTask extends AsyncTask<String, Void, String> {
    private static final String TAG = CalendarEventTask.class.getSimpleName();

    private static final boolean LOCAL_LOGD = true;
    private static final boolean LOCAL_LOGV = true;

    private static final Format FORMAT_TIME = new SimpleDateFormat("HH:mm");

    private Activity mActivity;

    public CalendarEventTask(Activity activity){
        mActivity = activity;
    }

    @Override
    protected String doInBackground(String... args) {
        if (LOCAL_LOGV) Log.v(TAG, "doInBackground(): in: args"+Arrays.asList(args));

        String calendar = args[0];
        String title = args[1];

        try {
            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            CalendarAccessor.Event event = new CalendarAccessor.Event(title, startTime, endTime);
            boolean writeEventFlag = true;
            new CalendarAccessor(mActivity).addEvent(calendar, event, writeEventFlag);
            notify(null, DEFAULT_NOTIFICATION_URI, new long[]{0,200});
            return FORMAT_TIME.format(new Date(startTime))+" "+event.title();
        } catch (CalendarAccessException e) {
            notify(null, null, new long[]{0,200,100,200});
            return e.getMessage();
        }
    }

    @Override
    public void onPostExecute(String result) {
        if (LOCAL_LOGV) Log.v(TAG, "onPostExecute(): in: result="+result);
        Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
    }

    private void notify(String ticker, Uri sound, long[] vibrate)
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
}
