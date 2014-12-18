package io.dwak.tracker;

import java.util.ArrayList;

/**
 * Created by vishnu on 12/17/14.
 */
public class TrackerComputation {
    private final int mId;
    private final ArrayList<TrackerComputationFunction> mInvalidateCallbacks;
    private final TrackerComputation mParent;
    private final TrackerComputationFunction mFunction;
    private boolean mStopped;
    private boolean mInvalidated;
    private boolean mRecomputing;
    private boolean mFirstRun;
    private boolean mErrored;
    private boolean mConstructingComputation = false;

    TrackerComputation(TrackerComputationFunction function, TrackerComputation parent) {
        mStopped = false;
        mInvalidated = false;
        mId = Tracker.nextId++;
        mInvalidateCallbacks = new ArrayList<TrackerComputationFunction>();
        mFirstRun = true;
        mParent = parent;
        mFunction = function;
        mRecomputing = false;

        try {
            compute();
            mErrored = false;
        } finally {
            mFirstRun = false;
            if (mErrored) {
                stop();
            }

        }
    }

    void stop() {
        if (mStopped) {
            mStopped = true;
            invalidate();
        }
    }

    void invalidate() {
        if (!mInvalidated) {
            // if we're currently in _recompute(), don't enqueue
            // ourselves, since we'll rerun immediately anyway.
            if (!mRecomputing && !mStopped) {
                Tracker.getInstance().requireFlush();
                Tracker.getInstance().getPendingTrackerComputations().add(this);
            }

            mInvalidated = true;

            // callbacks can't add callbacks, because
            // self.invalidated === true.
            for (final TrackerComputationFunction invalidateCallback : mInvalidateCallbacks) {
                Tracker.getInstance().nonReactive(new TrackerComputationFunction() {
                    @Override
                    public void callback() {
                        invalidateCallback.callback();
                    }
                });
            }
            mInvalidateCallbacks.clear();
        }
    }

    void onInvalidate(TrackerComputationFunction callback) {
        mInvalidateCallbacks.add(callback);
    }

    private void compute() {
        mInvalidated = false;
        final TrackerComputation previousTrackerComputation = Tracker.getInstance().getCurrentTrackerComputation();
        Tracker.getInstance().setCurrentTrackerComputation(this);
        boolean previousInCompute = Tracker.getInstance().isInCompute();
        Tracker.getInstance().setInCompute(true);
        mFunction.callback();
        Tracker.getInstance().setCurrentTrackerComputation(previousTrackerComputation);
        Tracker.getInstance().setInCompute(false);
    }

    void reCompute() {
        mRecomputing = true;
        try {
            while (mInvalidated && !mStopped) {
                try {
                    compute();
                } catch (Exception e) {

                }
                // If _compute() invalidated us, we run again immediately.
                // A computation that invalidates itself indefinitely is an
                // infinite loop, of course.
                //
                // We could put an iteration counter here and catch run-away
                // loops.
            }
        } finally {
            mRecomputing = false;
        }

    }

    public int getId() {
        return mId;
    }
}
