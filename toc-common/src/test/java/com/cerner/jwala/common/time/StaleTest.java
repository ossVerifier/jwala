package com.cerner.jwala.common.time;

import org.junit.Before;
import org.junit.Test;

import com.cerner.jwala.common.time.Stale;
import com.cerner.jwala.common.time.TimeDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StaleTest {

    private Long timePeriod;
    private TimeUnit timeUnit;
    private Stale stale;

    @Before
    public void setup() {
        timePeriod = 10L;
        timeUnit = TimeUnit.SECONDS;
        stale = new Stale(new TimeDuration(timePeriod,
                                           timeUnit));
    }

    @Test
    public void testIsStale() throws Exception {

        final long lastAccessTime = System.currentTimeMillis();
        final boolean shouldBeStale = stale.isStale(lastAccessTime,
                                                    relativeToExpiration(lastAccessTime,
                                                                         1));
        assertTrue(shouldBeStale);
    }

    @Test
    public void testIsNotStale() throws Exception {
        final long lastAccessTime = System.currentTimeMillis();
        final boolean wasStale = stale.isStale(lastAccessTime,
                                               relativeToExpiration(lastAccessTime,
                                                                    -1));
        assertFalse(wasStale);
    }

    private long relativeToExpiration(final long aReferencePoint,
                                      final long anOffset) {
        return aReferencePoint + TimeUnit.MILLISECONDS.convert((timePeriod + anOffset),
                                                               timeUnit);
    }
}
