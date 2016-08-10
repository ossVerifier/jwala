package com.cerner.jwala.common.time;

import java.util.concurrent.TimeUnit;

public class TimeRemainingCalculator {

    private final long expirationPoint;

    public TimeRemainingCalculator(final TimeDuration theDuration) {
        this(theDuration,
             System.currentTimeMillis());
    }

    public TimeRemainingCalculator(final TimeDuration theDuration,
                                   final long theReferencePoint) {
        expirationPoint = theReferencePoint + theDuration.valueOf(TimeUnit.MILLISECONDS);
    }

    public TimeRemaining getTimeRemaining() {
        return getTimeRemaining(System.currentTimeMillis());
    }

    public TimeRemaining getTimeRemaining(final long aPointInTime) {
        return new TimeRemaining(calculateRemaining(aPointInTime));
    }

    protected TimeDuration calculateRemaining(final long aPointInTime) {
        return new TimeDuration(expirationPoint - aPointInTime,
                                TimeUnit.MILLISECONDS);
    }
}
