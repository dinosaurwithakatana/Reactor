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

}
