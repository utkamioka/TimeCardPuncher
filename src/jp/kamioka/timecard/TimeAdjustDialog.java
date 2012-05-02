package jp.kamioka.timecard;

import java.text.Format;
import java.text.SimpleDateFormat;

import jp.kamioka.timecard.event.TimeAdjusterEvent;
import jp.kamioka.timecard.event.TimeAdjusterListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class TimeAdjustDialog implements OnClickListener, DialogInterface.OnClickListener {
    private static final String TAG = TimeAdjustDialog.class.getSimpleName();
    private static final Format FORMAT_TIME = new SimpleDateFormat("HH:mm");
    private static final boolean LOCAL_LOGV = false;

    private Context mContext;
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private long mTime;
    private long mAdjustTime;
    private int mTimeOffset = 0;
    private ImageButton mPlusButton;
    private ImageButton mMinusButton;
    private EditText mEditText;
    private Vibrator mVibrator;
    private TimeAdjusterListener mTimeAdjusterListener;

    public TimeAdjustDialog(Context context, long time) {
        mContext = context;
        mTime = time;
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.timeadjuster, null);
        mPlusButton = (ImageButton)view.findViewById(R.id.time_adjust_btn_plus);
        mPlusButton.setOnClickListener(this);
        mMinusButton = (ImageButton)view.findViewById(R.id.time_adjust_btn_minus);
        mMinusButton.setOnClickListener(this);
        mEditText = (EditText)view.findViewById(R.id.time_adjust_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setIcon(android.R.drawable.ic_lock_idle_alarm);
        builder.setTitle(FORMAT_TIME.format(mTime));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        mBuilder = builder;
    }

    public AlertDialog show() {
        mDialog = mBuilder.show();
        _updateTime();
        return mDialog;
    }

    public TimeAdjustDialog setAdjustButton(int id, TimeAdjusterListener listener) {
        mBuilder.setPositiveButton(id, this);
        mTimeAdjusterListener = listener;
        return this;
    }

    @Override
    public void onClick(View view) {
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): view="+view);
        if (view==mPlusButton||view==mMinusButton) {
            mVibrator.vibrate(20);
            mTimeOffset+=(view==mPlusButton)?(+1):(-1);
            _updateTime();
            return;
        }
        return;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (LOCAL_LOGV) Log.v(TAG, "onClick(): dialog="+dialog+", which="+which);
        switch (which) {
        case -1:
            if (mTimeAdjusterListener!=null) {
                TimeAdjusterEvent event = new TimeAdjusterEvent(this, mAdjustTime, _offsetMillis());
                mTimeAdjusterListener.onTimeAdjusted(event);
            }
            break;
        }
    }

    private void _updateTime() {
        mEditText.setText(""+mTimeOffset);
        mAdjustTime = mTime+_offsetMillis();
        mDialog.setTitle(FORMAT_TIME.format(mAdjustTime));
    }

    private long _offsetMillis() {
        return mTimeOffset*60*1000;
    }
}
