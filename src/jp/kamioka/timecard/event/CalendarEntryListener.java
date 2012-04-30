package jp.kamioka.timecard.event;

public interface CalendarEntryListener {
    public void calendarEntryAdded(CalendarEntryEvent event);
    public void calendarEntryRemoved(CalendarEntryEvent evnet);
}
