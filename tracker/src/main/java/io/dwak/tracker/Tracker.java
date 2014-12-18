package io.dwak.tracker;

import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Tracker {
    static boolean mActive = false;
    private static Tracker sInstance;
    private static int nextId = 1;
    private TrackerComputation mCurrentComputation = null;
    private ArrayList<TrackerComputation> mPendingComputations;
    private boolean mWillFlush = false;
    private boolean mInFlush = false;
    private boolean mInCompute = false;
    private boolean mThrowFirstError = false;
    private ArrayList<TrackerComputationFunction> mTrackerFlushCallbacks;

    public Tracker() {
        mPendingComputations = new ArrayList<TrackerComputation>();
        mTrackerFlushCallbacks = new ArrayList<TrackerComputationFunction>();
        sInstance = this;
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
            // TODO
//            setTimeout(Tracker.flush, 0);
            mWillFlush = true;
        }
    }

    public TrackerComputation getCurrentComputation() {
        return mCurrentComputation;
    }

    public void setCurrentComputation(TrackerComputation currentComputation) {
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
                for (TrackerComputation pendingComputation : mPendingComputations) {
                    pendingComputation.reCompute();
                }

                if (!mTrackerFlushCallbacks.isEmpty()) {
                    final TrackerComputationFunction function = mTrackerFlushCallbacks.remove(0);
                    try {
                        function.onCompute();
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
        final TrackerComputation trackerComputation = new TrackerComputation(function, mCurrentComputation);

        if (mActive) {
            onInvalidate(new TrackerComputationFunction() {
                @Override
                public void onCompute() {
                    trackerComputation.stop();
                }
            });
        }

        return trackerComputation;
    }

    public TrackerComputationFunction nonReactive(TrackerComputationFunction function) {
        final TrackerComputation previous = mCurrentComputation;
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

    public ArrayList<TrackerComputation> getPendingComputations() {
        return mPendingComputations;
    }

    public static class TrackerComputation {
        private final TrackerComputation sInstance;
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
            sInstance = this;
            mStopped = false;
            mInvalidated = false;
            mId = nextId++;
            mInvalidateCallbacks = new ArrayList<TrackerComputationFunction>();
            mFirstRun = true;
            mParent = parent;
            mFunction = function;
            mRecomputing = false;

            try {
                function.onCompute();
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
                mInvalidateCallbacks.clear();
            }
        }

        private void onInvalidate(TrackerComputationFunction callback) {
            mInvalidateCallbacks.add(callback);
        }

        private void compute() {
            mInvalidated = false;
            final TrackerComputation previousComputation = Tracker.getInstance().getCurrentComputation();
            Tracker.getInstance().setCurrentComputation(this);
            boolean previousInCompute = Tracker.getInstance().isInCompute();
            Tracker.getInstance().setInCompute(true);
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

    public static class TrackerDependency {
        private SparseArray<TrackerComputation> mDependentsById;
        private TrackerComputation mComputation;

        public TrackerDependency() {
            mDependentsById = new SparseArray<TrackerComputation>();
        }

        public boolean depend(TrackerComputation computation) {
            if (computation == null) {
                if (!mActive)
                    return false;

                mComputation = Tracker.getInstance().getCurrentComputation();
            }

            final int id = computation.getId();
            if (mDependentsById.get(id) == null) {
                mDependentsById.put(id, computation);
                computation.onInvalidate(new TrackerComputationFunction() {
                    @Override
                    public void onCompute() {
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
