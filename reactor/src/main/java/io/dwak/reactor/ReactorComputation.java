package io.dwak.reactor;

import android.util.Log;

import java.util.ArrayList;

/**
 * A Computation object represents code that is repeatedly rerun
 * in response to
 * reactive data changes. Computations don't have return values; they just
 * perform actions, such as rerendering a template on the screen. Computations
 * are created using {@link Reactor#autoRun(ReactorComputationFunction)}.
 * Use {@link #stop()} to prevent further rerunning of a
 * computation.
 */
public class ReactorComputation {
    private final int mId;
    private final ArrayList<ReactorComputationFunction> mInvalidateCallbacks;
    private final ReactorComputation mParent;
    private final ReactorComputationFunction mFunction;

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
     * True during the initial run of the computation at the time {@link Reactor#autoRun(ReactorComputationFunction)}
     * is called, and false on subsequent reruns and at other times.
     */
    private boolean mFirstRun;
    private boolean mErrored;
    private boolean mConstructingComputation = false;

    ReactorComputation(ReactorComputationFunction function, ReactorComputation parent) {
        mStopped = false;
        mInvalidated = false;
        mId = Reactor.nextId++;
        mInvalidateCallbacks = new ArrayList<ReactorComputationFunction>();
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
                Reactor.getInstance().requireFlush();
                Reactor.getInstance().getPendingReactorComputations().add(this);
            }

            mInvalidated = true;

            // callbacks can't add callbacks, because
            // self.invalidated === true.
            for (final ReactorComputationFunction invalidateCallback : mInvalidateCallbacks) {
                Reactor.getInstance().nonReactive(new ReactorComputationFunction() {
                    @Override
                    public void react() {
                        invalidateCallback.react();
                    }
                });
            }
            mInvalidateCallbacks.clear();
        }
    }

    /**
     * Registers a {@link ReactorComputationFunction} to run when this computation is next invalidated,
     * or runs it immediately if the computation is already invalidated.
     * The react is run exactly once and not upon future invalidations unless {@link #addInvalidateComputationFunction(ReactorComputationFunction)}
     * is called again after the computation becomes valid again.
     */
    public void addInvalidateComputationFunction(ReactorComputationFunction callback) {
        mInvalidateCallbacks.add(callback);
    }

    private void compute() {
        mInvalidated = false;
        final ReactorComputation previousReactorComputation = Reactor.getInstance().getCurrentReactorComputation();
        Reactor.getInstance().setCurrentReactorComputation(this);
        boolean previousInCompute = Reactor.getInstance().isInCompute();
        Reactor.getInstance().setInCompute(true);
        mFunction.react();
        Reactor.getInstance().setCurrentReactorComputation(previousReactorComputation);
        Reactor.getInstance().setInCompute(false);

        if (Reactor.getInstance().shouldLog()) {
            Log.d(ReactorComputation.class.getSimpleName(), this.toString());
        }
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

    @Override
    public String toString() {
        return "TrackerComputation{" +
                "mId=" + mId +
                ", mInvalidateCallbacks=" + mInvalidateCallbacks +
                ", mParent=" + mParent +
                ", mFunction=" + mFunction +
                ", mStopped=" + mStopped +
                ", mInvalidated=" + mInvalidated +
                ", mRecomputing=" + mRecomputing +
                ", mFirstRun=" + mFirstRun +
                ", mErrored=" + mErrored +
                ", mConstructingComputation=" + mConstructingComputation +
                "}";
    }
}
