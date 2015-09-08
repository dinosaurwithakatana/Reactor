package io.dwak.reactor;

/**
 * Controls the verbosity of logging
 */
public enum LogLevel {
    /**
     * No logging
     */
    NONE(0),

    /**
     * Only log when a computation is in compute
     */
    COMPUTE(1),

    /**
     * Log all computation actions
     */
    ALL(2);

    public final int logLevelValue;

    LogLevel(int i) {
        logLevelValue = i;
    }
}
