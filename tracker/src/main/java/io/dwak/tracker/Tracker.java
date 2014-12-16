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
    private ArrayList<TrackerFlushCallbacks> mTrackerFlushCallbackses;

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

    interface TrackerInvalidateCallbacks {
        void onInvalidate();
    }

    public static class TrackerComputation {
        private final TrackerComputation sInstance;
        private final boolean mStopped;
        private final boolean mInvalidated;
        private final int mId;
        private final ArrayList<TrackerInvalidateCallbacks> mInvalidateCallbacks;
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
            mInvalidateCallbacks = new ArrayList<TrackerInvalidateCallbacks>();
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

        private void onInvalidate(TrackerInvalidateCallbacks callbacks) {

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
                computation.onInvalidate(new TrackerInvalidateCallbacks() {
                    @Override
                    public void onInvalidate() {
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
