package io.dwak.reactor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;

import io.dwak.reactor.interfaces.ReactorComputationFunction;
import io.dwak.reactor.interfaces.ReactorFlushCallback;
import io.dwak.reactor.interfaces.ReactorInvalidateCallback;

/**
 * See https://www.meteor.com/tracker for more documentation on the source library
 */
public class Reactor {

    public static final String TAG = "Reactor";

    /**
     * True if there is a current computation, meaning that dependencies on reactive data sources
     * will be tracked and potentially cause the current computation to be rerun.
     */
    static boolean mActive = false;

    /**
     * Tracks the next id for a computation
     */
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
     * `true` if a {@link #flush()} is scheduled, or if we are in {@link #flush()} now
     */
    private boolean mWillFlush = false;

    /**
     * `true` if we are in {@link #flush()} now
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
     * Collection of callbacks to call on flush
     */
    private ArrayList<ReactorFlushCallback> mFlushCallbacks;

    /**
     * Log verbosity, {@link LogLevel#NONE} by default
     */
    private LogLevel mLogLevel = LogLevel.NONE;

    Reactor() {
        mPendingReactorComputations = new ArrayDeque<ReactorComputation>();
        mFlushCallbacks = new ArrayList<ReactorFlushCallback>();
    }

    public static Reactor getInstance() {
        if (sInstance == null) {
            sInstance = new Reactor();
        }
        return sInstance;
    }

    public void requireFlush() {
        if (!mWillFlush) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    flush();
                }
            });
            mWillFlush = true;
        }
    }

    /**
     * Process all reactive updates immediately and ensure that all invalidated computations are rerun.
     */
    public void flush() {
        if(getLogLevel() == LogLevel.ALL){
            Log.d(TAG, "Reactor is flushing");
        }
        if (mInFlush) {
            throw new IllegalStateException("Can't call Reactor.flush while flushing");
        }

        if (mInCompute) {
            throw new IllegalStateException("Can't flush inside Reactor.autoRun");
        }

        mInFlush = true;
        mWillFlush = true;

        boolean finishedTry = false;
        try {
            while (!mPendingReactorComputations.isEmpty() || !mFlushCallbacks.isEmpty()) {
                while(!mPendingReactorComputations.isEmpty()) {
                    mPendingReactorComputations.remove().reCompute();
                }

                if (!mFlushCallbacks.isEmpty()) {
                    final ReactorFlushCallback function = mFlushCallbacks.remove(0);
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
                Reactor.getInstance().flush();
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
     * @return the {@link ReactorComputation} reference
     */
    public ReactorComputation autoRun(ReactorComputationFunction function) {
        if(function == null){
            throw new NullPointerException("function cannot be null!");
        }
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
        mFlushCallbacks.add(function);
        requireFlush();
    }

    ReactorComputation getCurrentReactorComputation() {
        return mCurrentReactorComputation;
    }

    void setCurrentReactorComputation(ReactorComputation currentReactorComputation) {
        mCurrentReactorComputation = currentReactorComputation;
        mActive = currentReactorComputation != null;
    }

    ArrayDeque<ReactorComputation> getPendingReactorComputations() {
        return mPendingReactorComputations;
    }

    public boolean isInCompute() {
        return mInCompute;
    }

    void setInCompute(boolean inCompute) {
        mInCompute = inCompute;
    }

    /**
     * Explicitly set the log level
     * @param logLevel level to set
     */
    public void setLogLevel(LogLevel logLevel){
        mLogLevel = logLevel;
    }

    /**
     * Sets LogLevel to {@link LogLevel#ALL}
     * @param shouldLog true if log level should be changed to ALL
     */
    public void setShouldLog(boolean shouldLog) {
        mLogLevel = shouldLog ? LogLevel.ALL : LogLevel.NONE;
    }

    boolean shouldLog() {
        return mLogLevel != LogLevel.NONE;
    }

    LogLevel getLogLevel(){
        return mLogLevel;
    }

}
