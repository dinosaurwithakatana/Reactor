package io.dwak.tracker;

import java.util.ArrayList;

/**
 * A Computation object represents code that is repeatedly rerun
 * in response to
 * reactive data changes. Computations don't have return values; they just
 * perform actions, such as rerendering a template on the screen. Computations
 * are created using {@link io.dwak.tracker.Tracker#autoRun(TrackerComputationFunction)}.
 * Use {@link #stop()} to prevent further rerunning of a
 * computation.
 */
public class TrackerComputation {
    private final int mId;
    private final ArrayList<TrackerComputationFunction> mInvalidateCallbacks;
    private final TrackerComputation mParent;
    private final TrackerComputationFunction mFunction;

    /**
     * true if this computation has been stopped
     */
    private boolean mStopped;

    /**
     * true if this computation has been invalidated or stopped
     */
    private boolean mInvalidated;
    private boolean mRecomputing;

    /**
     * True during the initial run of the computation at the time {@link io.dwak.tracker.Tracker#autoRun(TrackerComputationFunction)}
     * is called, and false on subsequent reruns and at other times.
     */
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

    /**
     * Prevents this computation from rerunning.
     */
    public void stop() {
        if (!mStopped) {
            mStopped = true;
            invalidate();
        }
    }

    /**
     * Invalidates this computation so that it will be rerun.
     */
    public void invalidate() {
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

    /**
     * Registers a {@link io.dwak.tracker.TrackerComputationFunction} to run when this computation is next invalidated,
     * or runs it immediately if the computation is already invalidated.
     * The callback is run exactly once and not upon future invalidations unless {@link #addInvalidateComputationFunction(TrackerComputationFunction)}
     * is called again after the computation becomes valid again.
     */
    public void addInvalidateComputationFunction(TrackerComputationFunction callback) {
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
