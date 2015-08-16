package io.dwak.reactor;

/**
 * Controls the verbosity of logging
 */
public enum LogLevel {
    /**
     * No logging
     */
    NONE,

    /**
     * Only log when a computation is in compute
     */
    COMPUTE,

    /**
     * Log all computation actions
     */
    ALL
}
