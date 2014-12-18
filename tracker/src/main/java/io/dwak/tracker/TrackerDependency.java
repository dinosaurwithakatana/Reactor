package io.dwak.tracker;

import android.util.SparseArray;

/**
 * Created by vishnu on 12/17/14.
 */
public class TrackerDependency {
    private SparseArray<TrackerComputation> mDependentsById;

    public TrackerDependency() {
        mDependentsById = new SparseArray<TrackerComputation>();
    }

    public boolean depend() {
        return depend(null);
    }

    public boolean depend(TrackerComputation trackerComputation) {
        if (trackerComputation == null) {
            if (!Tracker.mActive)
                return false;

            trackerComputation = Tracker.getInstance().getCurrentTrackerComputation();
        }

        final int id = trackerComputation.getId();
        if (mDependentsById.get(id) == null) {
            mDependentsById.put(id, trackerComputation);
            trackerComputation.onInvalidate(new TrackerComputationFunction() {
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
