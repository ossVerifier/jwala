package com.cerner.jwala.common.time;

public class Stale {

    private final TimeDuration time;

    public Stale(final TimeDuration theStaleTime) {
        time = theStaleTime;
    }

    public boolean isStale(final long aLastAccessTime) {
        return isStale(aLastAccessTime,
                       System.currentTimeMillis());
    }

    boolean isStale(final long aLastAccessTime,
                    final long aReferencePoint) {
        final TimeRemaining timeRemaining = new TimeRemainingCalculator(time,
                                                                        aLastAccessTime).getTimeRemaining(aReferencePoint);
        return !timeRemaining.isTimeRemaining();
    }
}
