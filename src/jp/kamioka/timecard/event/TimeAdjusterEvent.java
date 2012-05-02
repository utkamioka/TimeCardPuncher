package jp.kamioka.timecard.event;

import java.util.EventObject;

public class TimeAdjusterEvent extends EventObject {
    private long mTime;
    private long mOffset;
    public TimeAdjusterEvent(Object source, long time, long offset) {
        super(source);
        mTime = time;
        mOffset = offset;
    }
    public long[] getTimeAndOffset() {
        return new long[]{mTime,mOffset};
    }
}
