package io.dwak.tracker;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Tracker {
    static boolean mActive = false;
    static int nextId = 1;
    private static Tracker sInstance;
    private TrackerComputation mCurrentTrackerComputation = null;
    private ArrayDeque<TrackerComputation> mPendingTrackerComputations;
    private boolean mWillFlush = false;
    private boolean mInFlush = false;
    private boolean mInCompute = false;
    private boolean mThrowFirstError = false;
    private ArrayList<TrackerComputationFunction> mTrackerFlushCallbacks;

    Tracker() {
        mPendingTrackerComputations = new ArrayDeque<TrackerComputation>();
        mTrackerFlushCallbacks = new ArrayList<TrackerComputationFunction>();
        sInstance = this;
    }

    public static Tracker init() {
        return new Tracker();
    }

    public static Tracker getInstance() {
        return sInstance;
    }

    public boolean isInCompute() {
        return mInCompute;
    }

    public void setInCompute(boolean inCompute) {
        mInCompute = inCompute;
    }

    public void requireFlush() {
        if (!mWillFlush) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    flush(false);
                }
            });
            mWillFlush = true;
        }
    }

    public TrackerComputation getCurrentTrackerComputation() {
        return mCurrentTrackerComputation;
    }

    public void setCurrentTrackerComputation(TrackerComputation currentTrackerComputation) {
        mCurrentTrackerComputation = currentTrackerComputation;
        mActive = currentTrackerComputation != null;
    }

    public void flush(boolean throwFirstError) {
        if (mInFlush) {
            throw new RuntimeException("Can't call Tracker.flush while flushing");
        }

        if (mInCompute) {
            throw new RuntimeException("Can't flush inside Tracker.autoRun");
        }

        mInFlush = true;
        mWillFlush = true;
        mThrowFirstError = throwFirstError;

        boolean finishedTry = false;
        try {
            while (!mPendingTrackerComputations.isEmpty() || !mTrackerFlushCallbacks.isEmpty()) {
                mPendingTrackerComputations.remove().reCompute();

                if (!mTrackerFlushCallbacks.isEmpty()) {
                    final TrackerComputationFunction function = mTrackerFlushCallbacks.remove(0);
                    try {
                        function.callback();
                    } catch (Exception e) {
                    }
                }
            }
            finishedTry = true;
        } finally {
            if (!finishedTry) {
                mInFlush = false;
                Tracker.getInstance().flush(false);
            }

            mWillFlush = false;
            mInFlush = false;
        }
    }

    public TrackerComputation autoRun(TrackerComputationFunction function) {
        final TrackerComputation trackerTrackerComputation = new TrackerComputation(function, mCurrentTrackerComputation);

        if (mActive) {
            onInvalidate(new TrackerComputationFunction() {
                @Override
                public void callback() {
                    trackerTrackerComputation.stop();
                }
            });
        }

        return trackerTrackerComputation;
    }

    public TrackerComputationFunction nonReactive(TrackerComputationFunction function) {
        final TrackerComputation previous = getCurrentTrackerComputation();
        setCurrentTrackerComputation(null);
        try {
            return function;
        } finally {
            setCurrentTrackerComputation(previous);
        }
    }

    public void onInvalidate(TrackerComputationFunction function) {
        if (!mActive) {
            throw new RuntimeException("Tracker.onInvalidate requires a currentComputation");
        }

        mCurrentTrackerComputation.onInvalidate(function);
    }

    public void afterFlush(TrackerComputationFunction function) {
        mTrackerFlushCallbacks.add(function);
        requireFlush();
    }

    public ArrayDeque<TrackerComputation> getPendingTrackerComputations() {
        return mPendingTrackerComputations;
    }
}
