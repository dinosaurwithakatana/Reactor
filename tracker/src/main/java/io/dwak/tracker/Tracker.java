package io.dwak.tracker;

import java.util.ArrayList;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class Tracker {
    private boolean mActive = false;
    private TrackerComputation mCurrentComputation = null;
    private int nextId = 1;
    private ArrayList<TrackerComputation> mPendingComputations;
    private boolean mWillFlush = false;
    private boolean mInFlush = false;
    private boolean mInCompute = false;
    private boolean mThrowFirstError = false;
    private ArrayList<TrackerFlushCallbacks> mTrackerFlushCallbackses;

    public Tracker() {
    }

    public void setCurrentComputation(TrackerComputation currentComputation) {
        mCurrentComputation = currentComputation;
        mActive = currentComputation != null;
    }

    public void requireFlush(){
        if(!mWillFlush){
            // TODO
//            setTimeout(Tracker.flush, 0);
            mWillFlush = true;
        }
    }

    public class TrackerComputation {
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

        private void compute() {

        }

        private void reCompute() {

        }
    }

    private class TrackerInvalidateCallbacks {
    }
}
