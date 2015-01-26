package io.dwak.reactor;

/**
 * Created by vishnu on 1/25/15.
 */
public class ReactorVar<T> {
    private T mValue;
    private ReactorDependency mDependency = new ReactorDependency();

    public T getValue() {
        mDependency.depend();
        return mValue;
    }

    public void setValue(T value) {
        this.mValue = value;
        mDependency.changed();
    }

    public ReactorDependency getDependency() {
        return mDependency;
    }
}
