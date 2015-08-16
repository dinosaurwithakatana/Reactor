package io.dwak.reactor;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import io.dwak.reactor.interfaces.ReactorComputationFunction;
import io.dwak.reactor.interfaces.ReactorInvalidateCallback;

@RunWith(AndroidJUnit4.class)
public class ReactorTest {
    @Rule public final ExpectedException mException = ExpectedException.none();
    int x = 0;
    private String mBuf;

    @Before
    public void setup() {
        Reactor.getInstance().setLogLevel(LogLevel.ALL);
    }

    @Test
    public void reactorRun() {
        final ReactorDependency dependency = new ReactorDependency();
        ReactorComputation handle = Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react(ReactorComputation reactorComputation) {
                dependency.depend();
                ++x;
            }
        });

        Assert.assertEquals(x, 1);
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 1);
        dependency.changed();
        Assert.assertEquals(x, 1);
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 2);
        dependency.changed();
        Assert.assertEquals(x, 2);
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 3);
        dependency.changed();
        // Prevent the function from running further.
        handle.stop();
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 3);
        dependency.changed();
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 3);

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react(ReactorComputation reactorComputation) {
                dependency.depend();
                ++x;
                if (x == 6) {
                    reactorComputation.stop();
                }
            }
        });

        Assert.assertEquals(x, 4);
        dependency.changed();
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 5);
        dependency.changed();
        // Increment to 6 and stop.
        Reactor.getInstance().flush();
        Assert.assertEquals(x, 6);
        dependency.changed();
        Reactor.getInstance().flush();
        // Still 6!
        Assert.assertEquals(x, 6);

        mException.expect(NullPointerException.class);
        Reactor.getInstance().autoRun(null);
    }

    @Test
    public void nestedRun(){
        final ReactorDependency a = new ReactorDependency();
        final ReactorDependency b = new ReactorDependency();
        final ReactorDependency c = new ReactorDependency();
        final ReactorDependency d = new ReactorDependency();
        final ReactorDependency e = new ReactorDependency();
        final ReactorDependency f = new ReactorDependency();

        mBuf = "";

        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
            @Override
            public void react(ReactorComputation reactorComputation) {
                final ReactorComputation c1 = reactorComputation;
                a.depend();
                mBuf += 'a';
                Reactor.getInstance().autoRun(new ReactorComputationFunction() {
                    @Override
                    public void react(ReactorComputation reactorComputation) {
                        b.depend();
                        mBuf += 'b';
                        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
                            @Override
                            public void react(ReactorComputation reactorComputation) {
                                c.depend();
                                mBuf += 'c';
                                Reactor.getInstance().autoRun(new ReactorComputationFunction() {
                                    @Override
                                    public void react(ReactorComputation reactorComputation) {
                                        final ReactorComputation c2 = reactorComputation;
                                        d.depend();
                                        mBuf += 'd';

                                        Reactor.getInstance().autoRun(new ReactorComputationFunction() {
                                            @Override
                                            public void react(ReactorComputation reactorComputation) {
                                                e.depend();
                                                mBuf += 'e';

                                                Reactor.getInstance().autoRun(new ReactorComputationFunction() {
                                                    @Override
                                                    public void react(ReactorComputation reactorComputation) {
                                                        f.depend();
                                                        mBuf += 'f';
                                                    }
                                                });
                                            }
                                        });
                                        Reactor.getInstance().onInvalidate(new ReactorInvalidateCallback() {
                                            @Override
                                            public void onInvalidate() {
                                                c2.stop();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

                Reactor.getInstance().onInvalidate(new ReactorInvalidateCallback() {
                    @Override
                    public void onInvalidate() {
                        c1.stop();
                    }
                });
            }
        });

        expect("abcdef");

        b.changed();
        expect("");
        Reactor.getInstance().flush();
        expect("bcdef");

        c.changed();
        Reactor.getInstance().flush();
        expect("cdef");

        // should cause running
        changeAndExpect(e, "ef");
        changeAndExpect(f, "f");
        // invalidate inner context
        changeAndExpect(d, "");
        // no more running!
        changeAndExpect(e, "");
        changeAndExpect(f, "");
        // rerun C
        changeAndExpect(c, "cdef");
        changeAndExpect(e, "ef");
        changeAndExpect(f, "f");
        // rerun B
        changeAndExpect(b, "bcdef");
        changeAndExpect(e, "ef");
        changeAndExpect(f, "f");
        // kill A
        a.changed();
        changeAndExpect(f, "");
        changeAndExpect(e, "");
        changeAndExpect(d, "");
        changeAndExpect(c, "");
        changeAndExpect(b, "");
        changeAndExpect(a, "");

        Assert.assertFalse(a.hasDependants());
        Assert.assertFalse(b.hasDependants());
        Assert.assertFalse(c.hasDependants());
        Assert.assertFalse(d.hasDependants());
        Assert.assertFalse(e.hasDependants());
        Assert.assertFalse(f.hasDependants());
    }

    public void expect(String string){
        Assert.assertEquals(mBuf, string);
        mBuf = "";
    }

    public void changeAndExpect(ReactorDependency v, String str){
        v.changed();
        Reactor.getInstance().flush();
        expect(str);
    }
}
