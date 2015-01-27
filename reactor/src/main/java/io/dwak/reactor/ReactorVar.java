package io.dwak.reactor;

/**
 * Object that wraps an object and it's {@link io.dwak.reactor.ReactorDependency}
 * If you'd like to use primitive data types, you'll have to handle your own instance of
 * {@link io.dwak.reactor.ReactorDependency} alongside your primitive value
 * Created by vishnu on 1/25/15.
 */
public class ReactorVar<T> {
    private T mValue;
    private ReactorDependency mDependency = new ReactorDependency();

    public ReactorVar() {
    }

    public ReactorVar(T value) {
        mValue = value;
    }

    public T getValue() {
        if(mDependency !=null)
            mDependency.depend();
        return mValue;
    }

    public void setValue(T value) {
        this.mValue = value;
        if(mDependency != null)
            mDependency.changed();
    }

    public ReactorDependency getDependency() {
        return mDependency;
    }

    public void setDependency(ReactorDependency dependency) {
        mDependency = dependency;
    }
}
