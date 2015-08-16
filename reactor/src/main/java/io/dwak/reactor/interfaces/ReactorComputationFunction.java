package io.dwak.reactor.interfaces;

import io.dwak.reactor.ReactorComputation;

/**
 * Callback for reactor computations
 */
public interface ReactorComputationFunction {
    void react(ReactorComputation reactorComputation);
}
