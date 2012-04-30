package jp.kamioka.timecard;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CalendarEntryAdapter extends BaseAdapter {
    private static final String TAG = CalendarEntryAdapter.class.getSimpleName();
    private static final Format FORMAT_TIME = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private Context mContext;
    private List<CalendarEntry> mCalendarEntries = new ArrayList<CalendarEntry>();

    public CalendarEntryAdapter(Context context) {
        mContext = context;
    }

    public void addCalendarEntry(CalendarEntry entry) {
        mCalendarEntries.add(0, entry);
        notifyDataSetChanged();
    }

    public void removeCalendarEntry(CalendarEntry entry) {
        mCalendarEntries.remove(entry);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCalendarEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return mCalendarEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view==null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row, null);
        }
        CalendarEntry entry = (CalendarEntry)getItem(position);
        TextView timeView = (TextView)view.findViewById(R.id.text_time);
        TextView titleView = (TextView)view.findViewById(R.id.text_title);
        timeView.setText(FORMAT_TIME.format(entry.getTime()));
        titleView.setText(entry.getTitle());
        return view;
    }
}