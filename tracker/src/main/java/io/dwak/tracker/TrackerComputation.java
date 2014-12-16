package io.dwak.tracker;

/**
 * Created by vrajeevan on 12/16/14.
 */
public class TrackerComputation {
    private final TrackerComputation sInstance;
    private final boolean mStopped;
    private final boolean mInvalidated;
    private boolean mFirstRun;
    private final TrackerComputationFunction mFunction;
    private final boolean mRecomputing;
    private boolean mErrored;
    private boolean mConstructingComputation = false;

    TrackerComputation(TrackerComputationFunction function) {
        sInstance = this;
        mStopped = false;
        mInvalidated = false;
        mFirstRun = true;
        mFunction = function;
        mRecomputing = false;

        try{
            function.onCompute();
            mErrored = false;
        }
        finally {
            mFirstRun = false;
            if(mErrored){
                stop();
            }

        }
    }

    private void stop() {

    }

    public interface InvalidationCallback{
        void onInvalidate(TrackerComputationFunction f);
    }
}
