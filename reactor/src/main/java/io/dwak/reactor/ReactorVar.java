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
        if (mDependency == null)
            mDependency = new ReactorDependency();

        mDependency.depend();
        return mValue;
    }

    public void setValue(T value) {
        if(!value.equals(mValue)) {
            this.mValue = value;
            if (mDependency == null)
                mDependency = new ReactorDependency();

            mDependency.changed();
        }
    }

    public ReactorDependency getDependency() {
        return mDependency;
    }

    public void setDependency(ReactorDependency dependency) {
        mDependency = dependency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReactorVar that = (ReactorVar) o;

        if (mValue != null ? !mValue.equals(that.mValue) : that.mValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mValue != null ? mValue.hashCode() : 0;
    }
}
