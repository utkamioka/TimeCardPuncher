package jp.kamioka.timecard;

import android.net.Uri;

public class CalendarEntry {
    private String mCalendar;
    private long mTime;
    private String mTitle;
    private Uri mUri;
    private boolean mWriteFlag = true;

    public CalendarEntry(String calendar, long time, String title, Uri uri) {
        mCalendar = calendar;
        mTime = time;
        mTitle = title;
        mUri = uri;
    }
    public CalendarEntry(String calendar, long time, String title) {
        this(calendar, time, title, null);
    }

    public String getCalendar() { return mCalendar; }
    public long getTime() { return mTime; }
    public String getTitle() { return mTitle; }
    public void setUri(Uri uri) { mUri = uri; }
    public Uri getUri() { return mUri; }
    public void setWriteFlag(boolean flag) { mWriteFlag = flag; }
    public boolean getWriteFlag() { return mWriteFlag; }
}
