package jp.kamioka.timecard;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class CalendarAccessor
{
	private static final String TAG = "CalendarAccessor";
	private Activity mActivity;
	
	public CalendarAccessor(Activity a)
	{
		mActivity = a;
	}
	
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_ID = "_id";
	
	private static final String[] PROJECTION = new String[] { COLUMN_ID, COLUMN_NAME };
	private static final String SELECTION = "access_level" + "=?";
	private static final String[] SELECTION_ARGS = new String[] { "700" };

	//Froyo以前のOSの場合、content://calendar/calendarsを指定する
	private static final String AUTHORITY = "com.android.calendar";
	private static final Uri CALENDAR_URI = Uri.parse("content://"+AUTHORITY+"/calendars");
	private static final Uri EVENT_URI = Uri.parse("content://"+AUTHORITY+"/events");
	
	/**
	 * 指定カレンダーのIDを取得。
	 * @param name
	 * @return カレンダーのID
	 */
	public int getCalendarId(String name)
	{
		Cursor c = mActivity.managedQuery(CALENDAR_URI, PROJECTION, SELECTION, SELECTION_ARGS, null);
		if ( c.moveToFirst() ) {
			int nameColumn = c.getColumnIndex(COLUMN_NAME);
			int idColumn = c.getColumnIndex(COLUMN_ID);
			Log.d(TAG, "getCalendarId(): Columns: name="+nameColumn+", id="+idColumn);
			do {
				if ( c.getString(nameColumn).equals(name) ) return c.getInt(idColumn);
			} while ( c.moveToNext() );
		}
		return -1;
	}
	
	public String[] getCalendars()
	{
		Cursor c = mActivity.managedQuery(CALENDAR_URI, PROJECTION, SELECTION, SELECTION_ARGS, null);
		List<String> list = new ArrayList<String>();
		if (c.moveToFirst()) {
			int nameColumn = c.getColumnIndex(COLUMN_NAME);
			int idColumn = c.getColumnIndex(COLUMN_ID);
			do {
				String name = c.getString(nameColumn);
				int id = c.getInt(idColumn);
				Log.d(TAG, "getCalendars(): name="+name+", id="+id);
				list.add(c.getString(nameColumn));
			} while (c.moveToNext());
		}		
		return (String[])list.toArray(new String[0]);
	}
	
	public void addEvent(String calendarName, Event event)
	throws CalendarAccessException
	{
		int id = getCalendarId(calendarName);
		if ( id < 0 ) {
			Log.d(TAG, calendarName+": No such calendar.");
			throw new CalendarAccessException(calendarName+": No such calendar.");
		}
		Log.d(TAG, calendarName+"'s calendar ID: "+id);

		ContentValues values = event.toContentValues();
		values.put("calendar_id", id);

		ContentResolver contentResolver = mActivity.getContentResolver();
		Uri entry = contentResolver.insert(EVENT_URI, values);
		Log.d(TAG, "New calendar entry: "+entry);
	}
	
	public static class Event
	{
		private String mTitle;
		private String mDescription;
		private String mLocation;
		private long mStartTime;
		private long mEndTime;
		public Event(String title, long startTime, long endTime)
		{
			mTitle = title;
			mStartTime = startTime;
			mEndTime = endTime;
		}
		public void setDescription(String description) { mDescription = description; }
		public void setLocation(String location) { mLocation = location; }
		public String title() { return mTitle; }
		public String description() { return mDescription; }
		public String location() { return mLocation; }
		public long startTime() { return mStartTime; }
		public long endTime() { return mEndTime; }
		public ContentValues toContentValues()
		{
			ContentValues values = new ContentValues();
			values.put("title", title());
			values.put("description", description());
			values.put("eventLocation", location());
			values.put("dtstart", startTime());
			values.put("dtend", endTime());
			return values;
		}
		@Override
		public String toString()
		{
			StringBuilder s = new StringBuilder();
			s.append("Event[");
			s.append("title=").append(mTitle);
			s.append(",");
			s.append("description=").append(mDescription);
			s.append(",");
			s.append("location=").append(mLocation);
			s.append(",");
			s.append("startTime=").append(mStartTime);
			s.append(",");
			s.append("endTime=").append(mEndTime);
			s.append("]");
			return s.toString();
		}
	}
}
