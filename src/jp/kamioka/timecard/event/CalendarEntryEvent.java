package jp.kamioka.timecard.event;

import java.util.EventObject;

import jp.kamioka.timecard.CalendarEntry;

import android.net.Uri;

@SuppressWarnings("serial")
public class CalendarEntryEvent extends EventObject {
    private CalendarEntry mCalendarEntry;
//    private long mTime;
//    private String mTitle;
//    private Uri mUri = null;
    public CalendarEntryEvent(Object source, CalendarEntry entry) {
        super(source);
        mCalendarEntry = entry;
    }
//    public CalendarEntryEvent(Object source, long time, String title, Uri uri) {
//        super(source);
//        mTime = time;
//        mTitle = title;
//        mUri = uri;
//    }
//    public long getTime() { return mTime; }
//    public String getTitle() { return mTitle; }
//    public Uri getUri() { return mUri; }
    public CalendarEntry getCalendarEntry() { return mCalendarEntry; }
}
