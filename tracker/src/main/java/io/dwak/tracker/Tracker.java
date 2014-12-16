package io.dwak.tracker;

import java.util.ArrayList;
import java.util.HashMap;

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
        sInstance = this;
    }

    public static Tracker getInstance() {
        return sInstance;
    }

    public void requireFlush(){
        if(!mWillFlush){
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

    public static class TrackerComputation {
        private final TrackerComputation sInstance;
        private final boolean mStopped;
        private final boolean mInvalidated;
        private final int mId;
        private final ArrayList<TrackerComputationFunction> mInvalidateCallbacks;
        private final TrackerComputation mParent;
        private final TrackerComputationFunction mFunction;
        private final boolean mRecomputing;
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

        }

        private void onInvalidate() {

        }

        private void invalidate() {

        }

        private void onInvalidate(TrackerComputationFunction callbacks) {

        }

        private void compute() {

        }

        private void reCompute() {

        }

        public int getId() {
            return mId;
        }
    }

    public static class TrackerDependency {
        private HashMap<Integer, TrackerComputation> mDependentsById;
        private TrackerComputation mComputation;

        public TrackerDependency() {
            mDependentsById = new HashMap<Integer, TrackerComputation>();
        }

        public boolean depend(TrackerComputation computation) {
            if (computation == null) {
                if (!mActive)
                    return false;

                mComputation = Tracker.getInstance().getCurrentComputation();
            }

            final int id = computation.getId();
            if (!mDependentsById.containsKey(id)) {
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
            for (Integer id : mDependentsById.keySet()) {
                mDependentsById.get(id).invalidate();
            }
        }

        public boolean hasDependants() {
            for (Integer id : mDependentsById.keySet()) {
                return true;
            }
            return false;
        }
    }
}
