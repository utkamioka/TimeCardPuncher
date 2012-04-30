package jp.kamioka.timecard;

public class AsyncTaskResult<T> {
    private boolean mStatus = true;
    private T mContent = null;
    private Throwable mCause = null;
    public AsyncTaskResult(boolean status, T content) {
        mStatus = status;
        mContent = content;
        mCause = null;
    }
    public AsyncTaskResult(Throwable cause) {
        mStatus = false;
        mContent = null;
        mCause = cause;
    }
    public AsyncTaskResult(boolean status) {
        mStatus = status;
        mContent = null;
        mCause = null;
    }
    public boolean getStatus() { return mStatus; }
    public T getContent() { return mContent; }
    public Throwable getCause() { return mCause; }
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("AsyncTaskResult[");
        s.append("status=").append(mStatus);
        s.append(",");
        s.append("content=").append(mContent);
        s.append(",");
        s.append("cause=").append(mCause);
        s.append("]");
        return s.toString();
    }
}
