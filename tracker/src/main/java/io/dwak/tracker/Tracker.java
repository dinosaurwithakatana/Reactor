package io.dwak.tracker;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Tracker {
    static boolean mActive = false;
    private static Tracker sInstance;
    private static int nextId = 1;
    private Computation mCurrentComputation = null;
    private ArrayDeque<Computation> mPendingComputations;
    private boolean mWillFlush = false;
    private boolean mInFlush = false;
    private boolean mInCompute = false;
    private boolean mThrowFirstError = false;
    private ArrayList<TrackerComputationFunction> mTrackerFlushCallbacks;

    Tracker() {
        mPendingComputations = new ArrayDeque<Computation>();
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

    public Computation getCurrentComputation() {
        return mCurrentComputation;
    }

    public void setCurrentComputation(Computation currentComputation) {
        mCurrentComputation = currentComputation;
        mActive = currentComputation != null;
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
            while (!mPendingComputations.isEmpty() || !mTrackerFlushCallbacks.isEmpty()) {
                mPendingComputations.remove().reCompute();

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

    public Computation autoRun(TrackerComputationFunction function) {
        final Computation trackerComputation = new Computation(function, mCurrentComputation);

        if (mActive) {
            onInvalidate(new TrackerComputationFunction() {
                @Override
                public void callback() {
                    trackerComputation.stop();
                }
            });
        }

        return trackerComputation;
    }

    public TrackerComputationFunction nonReactive(TrackerComputationFunction function) {
        final Computation previous = getCurrentComputation();
        setCurrentComputation(null);
        try {
            return function;
        } finally {
            setCurrentComputation(previous);
        }
    }

    public void onInvalidate(TrackerComputationFunction function) {
        if (!mActive) {
            throw new RuntimeException("Tracker.onInvalidate requires a currentComputation");
        }

        mCurrentComputation.onInvalidate(function);
    }

    public void afterFlush(TrackerComputationFunction function) {
        mTrackerFlushCallbacks.add(function);
        requireFlush();
    }

    public ArrayDeque<Computation> getPendingComputations() {
        return mPendingComputations;
    }

    public static class Computation {
        private final int mId;
        private final ArrayList<TrackerComputationFunction> mInvalidateCallbacks;
        private final Computation mParent;
        private final TrackerComputationFunction mFunction;
        private boolean mStopped;
        private boolean mInvalidated;
        private boolean mRecomputing;
        private boolean mFirstRun;
        private boolean mErrored;
        private boolean mConstructingComputation = false;

        Computation(TrackerComputationFunction function, Computation parent) {
            mStopped = false;
            mInvalidated = false;
            mId = nextId++;
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

        private void stop() {
            if (mStopped) {
                mStopped = true;
                invalidate();
            }
        }

        private void invalidate() {
            if (!mInvalidated) {
                // if we're currently in _recompute(), don't enqueue
                // ourselves, since we'll rerun immediately anyway.
                if (!mRecomputing && !mStopped) {
                    Tracker.getInstance().requireFlush();
                    Tracker.getInstance().getPendingComputations().add(this);
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

        private void onInvalidate(TrackerComputationFunction callback) {
            mInvalidateCallbacks.add(callback);
        }

        private void compute() {
            mInvalidated = false;
            final Computation previousComputation = Tracker.getInstance().getCurrentComputation();
            Tracker.getInstance().setCurrentComputation(this);
            boolean previousInCompute = Tracker.getInstance().isInCompute();
            Tracker.getInstance().setInCompute(true);
            mFunction.callback();
            Tracker.getInstance().setCurrentComputation(previousComputation);
            Tracker.getInstance().setInCompute(false);
        }

        private void reCompute() {
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

    public static class Dependency {
        private SparseArray<Computation> mDependentsById;

        public Dependency() {
            mDependentsById = new SparseArray<Computation>();
        }

        public boolean depend() {
            return depend(null);
        }

        public boolean depend(Computation computation) {
            if (computation == null) {
                if (!mActive)
                    return false;

                computation = Tracker.getInstance().getCurrentComputation();
            }

            final int id = computation.getId();
            if (mDependentsById.get(id) == null) {
                mDependentsById.put(id, computation);
                computation.onInvalidate(new TrackerComputationFunction() {
                    @Override
                    public void callback() {
                        mDependentsById.remove(id);
                    }
                });

                return true;
            }
            return false;
        }

        public void changed() {
            int key;
            for (int i = 0; i < mDependentsById.size(); i++) {
                key = mDependentsById.keyAt(i);
                mDependentsById.get(key).invalidate();
            }
        }

        public boolean hasDependants() {
            return mDependentsById.size() > 0;
        }
    }
}
