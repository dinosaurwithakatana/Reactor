package io.dwak.reactor;

import android.util.SparseArray;

import io.dwak.reactor.interfaces.ReactorInvalidateCallback;

/**
 * A Dependency represents an atomic unit of reactive data that a
 * computation might depend on.
 * When the data changes, the computations are invalidated.
 */
public class ReactorDependency {
    private SparseArray<ReactorComputation> mDependentsById;

    public ReactorDependency() {
        mDependentsById = new SparseArray<ReactorComputation>();
    }

    public boolean depend() {
        return depend(null);
    }

    /**
     * Declares that the current computation (or {@link ReactorComputation} if given) depends on `dependency`.
     * The computation will be invalidated the next time `dependency` changes.
     * If there is no current computation and {@link #depend()}} is called with no arguments, it does nothing and returns false.
     *
     * @param reactorComputation computation that depends on this dependency
     * @return true if computation is a new dependant rather than an existing one
     */
    public boolean depend(ReactorComputation reactorComputation) {
        if (reactorComputation == null) {
            if (!Reactor.mActive)
                return false;

            reactorComputation = Reactor.getInstance().getCurrentReactorComputation();
        }

        final int id = reactorComputation.getId();
        if (mDependentsById.get(id) == null) {
            mDependentsById.put(id, reactorComputation);
            reactorComputation.addInvalidateComputationFunction(new ReactorInvalidateCallback() {
                @Override
                public void onInvalidate() {
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
     * True if this Dependency has one or more dependent {@link ReactorComputation},
     * which would be invalidated if this {@link ReactorDependency} were to change.
     * @return true if the dependency has dependants
     */
    public boolean hasDependants() {
        return mDependentsById.size() > 0;
    }
}
