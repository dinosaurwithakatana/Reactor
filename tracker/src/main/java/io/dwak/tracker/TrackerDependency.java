package io.dwak.tracker;

import android.util.SparseArray;

/**
 * A Dependency represents an atomic unit of reactive data that a
 * computation might depend on.
 * When the data changes, the computations are invalidated.
 */
public class TrackerDependency {
    private SparseArray<TrackerComputation> mDependentsById;

    public TrackerDependency() {
        mDependentsById = new SparseArray<TrackerComputation>();
    }

    public boolean depend() {
        return depend(null);
    }

    /**
     * Declares that the current computation (or {@link io.dwak.tracker.TrackerComputation} if given) depends on `dependency`.
     * The computation will be invalidated the next time `dependency` changes.
     * If there is no current computation and {@link #depend()}} is called with no arguments, it does nothing and returns false.
     *
     * @param trackerComputation computation that depends on this dependency
     * @return true if computation is a new dependant rather than an existing one
     */
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

    /**
     * Invalidate all dependent computations immediately and remove them as dependents.
     */
    public void changed() {
        int key;
        for (int i = 0; i < mDependentsById.size(); i++) {
            key = mDependentsById.keyAt(i);
            mDependentsById.get(key).invalidate();
        }
    }

    /**
     * True if this Dependency has one or more dependent {@link io.dwak.tracker.TrackerComputation},
     * which would be invalidated if this {@link io.dwak.tracker.TrackerDependency} were to change.
     * @return true if the dependency has dependants
     */
    public boolean hasDependants() {
        return mDependentsById.size() > 0;
    }
}
