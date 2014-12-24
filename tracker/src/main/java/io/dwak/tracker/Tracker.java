package io.dwak.tracker;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Tracker {
    /**
     * True if there is a current computation, meaning that dependencies on reactive data sources
     * will be tracked and potentially cause the current computation to be rerun.
     */
    static boolean mActive = false;

    static int nextId = 1;

    /**
     * Current instance of the {@link #Tracker()}
     */
    private static Tracker sInstance;

    /**
     * The current computation, or `null` if there isn't one.
     * The current computation is the {@link io.dwak.tracker.TrackerComputation} object created by the innermost active call to
     * {@link #autoRun(TrackerComputationFunction)}, and it's the computation that gains dependencies when reactive data sources
     * are accessed.
     */
    private TrackerComputation mCurrentTrackerComputation = null;

    /**
     * computations whose callbacks we should call at flush time
     */
    private ArrayDeque<TrackerComputation> mPendingTrackerComputations;

    /**
     * `true` if a {@link io.dwak.tracker.Tracker#flush(boolean)} is scheduled, or if we are in {@link #flush(boolean)} now
     */
    private boolean mWillFlush = false;

    /**
     * `true` if we are in {@link #flush(boolean)} now
     */
    private boolean mInFlush = false;

    /**
     * `true` if we are computing a computation now, either first time
     * or recompute.  This matches {@link #mActive} unless we are inside
     * {@link #nonReactive(TrackerComputationFunction)}, which nullfies currentComputation even though
     * an enclosing computation may still be running.
     */
    private boolean mInCompute = false;

    /**
     * `true` if the `throwFirstError` option was passed in to the call
     * to {@link #flush(boolean)} that we are in. When set, throw rather than log the
     * first error encountered while flushing. Before throwing the error,
     * finish flushing (from a finally block), logging any subsequent
     * errors.
     */
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
        if(sInstance == null){
            sInstance = new Tracker();
        }
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

    /**
     * Process all reactive updates immediately and ensure that all invalidated computations are rerun.
     *
     * @param throwFirstError
     */
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


    /**
     * Run a {@link io.dwak.tracker.TrackerComputationFunction} now and rerun it later whenever its dependencies change.
     * Returns a {@link io.dwak.tracker.TrackerComputation} object that can be used to stop or observe the rerunning.
     *
     * @param function function to run when dependencies change
     * @return the tracker computation reference
     */
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

    /**
     * Run a function without tracking dependencies.
     * @param function function to run in nonreactive
     * @return the reference to the computation function
     */
    public TrackerComputationFunction nonReactive(TrackerComputationFunction function) {
        final TrackerComputation previous = getCurrentTrackerComputation();
        setCurrentTrackerComputation(null);
        try {
            return function;
        } finally {
            setCurrentTrackerComputation(previous);
        }
    }

    /**
     * Registers a new {@link io.dwak.tracker.TrackerComputationFunction} callback on the current computation (which must exist),
     * to be called immediately when the current computation is invalidated or stopped.
     * @param function the function callback to be run on invalidation
     */
    public void onInvalidate(TrackerComputationFunction function) {
        if (!mActive) {
            throw new RuntimeException("Tracker.addInvalidateComputationFunction requires a currentComputation");
        }

        mCurrentTrackerComputation.addInvalidateComputationFunction(function);
    }

    public void afterFlush(TrackerComputationFunction function) {
        mTrackerFlushCallbacks.add(function);
        requireFlush();
    }

    public ArrayDeque<TrackerComputation> getPendingTrackerComputations() {
        return mPendingTrackerComputations;
    }
}
