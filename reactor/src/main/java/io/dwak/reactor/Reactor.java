package io.dwak.reactor;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayDeque;
import java.util.ArrayList;

import io.dwak.reactor.interfaces.ReactorComputationFunction;
import io.dwak.reactor.interfaces.ReactorFlushCallback;
import io.dwak.reactor.interfaces.ReactorInvalidateCallback;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Reactor {

    public static final String TAG = Reactor.class.getSimpleName();

    /**
     * True if there is a current computation, meaning that dependencies on reactive data sources
     * will be tracked and potentially cause the current computation to be rerun.
     */
    static boolean mActive = false;

    static int nextId = 1;

    /**
     * Current instance of the {@link #Reactor()}
     */
    private static Reactor sInstance;

    /**
     * The current computation, or `null` if there isn't one.
     * The current computation is the {@link ReactorComputation} object created by the innermost active call to
     * {@link #autoRun(io.dwak.reactor.interfaces.ReactorComputationFunction)}, and it's the computation that gains dependencies when reactive data sources
     * are accessed.
     */
    private ReactorComputation mCurrentReactorComputation = null;

    /**
     * computations whose callbacks we should call at flush time
     */
    private ArrayDeque<ReactorComputation> mPendingReactorComputations;

    /**
     * `true` if a {@link Reactor#flush(boolean)} is scheduled, or if we are in {@link #flush(boolean)} now
     */
    private boolean mWillFlush = false;

    /**
     * `true` if we are in {@link #flush(boolean)} now
     */
    private boolean mInFlush = false;

    /**
     * `true` if we are computing a computation now, either first time
     * or recompute.  This matches {@link #mActive} unless we are inside
     * {@link #nonReactive(io.dwak.reactor.interfaces.ReactorComputationFunction)}, which nullfies currentComputation even though
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

    private ArrayList<ReactorFlushCallback> mTrackerFlushCallbacks;
    private boolean mShouldLog;

    Reactor() {
        mPendingReactorComputations = new ArrayDeque<ReactorComputation>();
        mTrackerFlushCallbacks = new ArrayList<ReactorFlushCallback>();
        sInstance = this;
    }

    public static Reactor getInstance() {
        if (sInstance == null) {
            sInstance = new Reactor();
        }
        return sInstance;
    }

    public boolean isInCompute() {
        return mInCompute;
    }

    void setInCompute(boolean inCompute) {
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

    ReactorComputation getCurrentReactorComputation() {
        return mCurrentReactorComputation;
    }

    void setCurrentReactorComputation(ReactorComputation currentReactorComputation) {
        mCurrentReactorComputation = currentReactorComputation;
        mActive = currentReactorComputation != null;
    }

    /**
     * Process all reactive updates immediately and ensure that all invalidated computations are rerun.
     *
     * @param throwFirstError
     */
    public void flush(boolean throwFirstError) {
        if (mInFlush) {
            throw new IllegalStateException("Can't call Reactor.flush while flushing");
        }

        if (mInCompute) {
            throw new IllegalStateException("Can't flush inside Reactor.autoRun");
        }

        mInFlush = true;
        mWillFlush = true;
        mThrowFirstError = throwFirstError;

        boolean finishedTry = false;
        try {
            while (!mPendingReactorComputations.isEmpty() || !mTrackerFlushCallbacks.isEmpty()) {
                while(!mPendingReactorComputations.isEmpty()) {
                    mPendingReactorComputations.remove().reCompute();
                }

                if (!mTrackerFlushCallbacks.isEmpty()) {
                    final ReactorFlushCallback function = mTrackerFlushCallbacks.remove(0);
                    try {
                        function.onFlush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            finishedTry = true;
        } finally {
            if (!finishedTry) {
                mInFlush = false;
                Reactor.getInstance().flush(false);
            }

            mWillFlush = false;
            mInFlush = false;
        }
    }


    /**
     * Run a {@link io.dwak.reactor.interfaces.ReactorComputationFunction} now and rerun it later whenever its dependencies change.
     * Returns a {@link ReactorComputation} object that can be used to stop or observe the rerunning.
     *
     * @param function function to run when dependencies change
     * @return the tracker computation reference
     */
    public ReactorComputation autoRun(ReactorComputationFunction function) {
        final ReactorComputation trackerReactorComputation = new ReactorComputation(function, mCurrentReactorComputation);

        if (mActive) {
            onInvalidate(new ReactorInvalidateCallback() {
                @Override
                public void onInvalidate() {
                    trackerReactorComputation.stop();
                }
            });
        }

        return trackerReactorComputation;
    }

    /**
     * Run a function without tracking dependencies.
     *
     * @param function function to run in nonreactive
     * @return the reference to the computation function
     */
    public ReactorComputationFunction nonReactive(ReactorComputationFunction function) {
        final ReactorComputation previous = getCurrentReactorComputation();
        setCurrentReactorComputation(null);
        try {
            function.react(null);
            return function;
        } finally {
            setCurrentReactorComputation(previous);
        }
    }

    /**
     * Registers a new {@link ReactorComputationFunction} react on the current computation (which must exist),
     * to be called immediately when the current computation is invalidated or stopped.
     *
     * @param function the function react to be run on invalidation
     */
    public void onInvalidate(ReactorInvalidateCallback function) {
        if (!mActive) {
            throw new RuntimeException("Reactor.addInvalidateComputationFunction requires a currentComputation");
        }

        mCurrentReactorComputation.addInvalidateComputationFunction(function);
    }

    public void afterFlush(ReactorFlushCallback function) {
        mTrackerFlushCallbacks.add(function);
        requireFlush();
    }

    ArrayDeque<ReactorComputation> getPendingReactorComputations() {
        return mPendingReactorComputations;
    }

    public void setShouldLog(boolean shouldLog) {
        mShouldLog = shouldLog;
    }

    boolean shouldLog() {
        return mShouldLog;
    }

}
