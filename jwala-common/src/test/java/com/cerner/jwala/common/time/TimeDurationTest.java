package com.cerner.jwala.common.time;

import org.junit.Before;
import org.junit.Test;

import com.cerner.jwala.common.time.TimeDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TimeDurationTest {

    private TimeDuration threeSeconds;
    private Long threeThousandMilliseconds;

    @Before
    public void setup() {
        threeSeconds = new TimeDuration(3L,
                                        TimeUnit.SECONDS);

        threeThousandMilliseconds = TimeUnit.MILLISECONDS.convert(threeSeconds.valueOf(),
                                                                  threeSeconds.getUnit());
    }

    @Test
    public void testConvertTo() throws Exception {
        final TimeDuration threeSecondsConvertedToMilliseconds = threeSeconds.convertTo(TimeUnit.MILLISECONDS);

        assertEquals(threeThousandMilliseconds,
                     threeSecondsConvertedToMilliseconds.valueOf());

    }

    @Test
    public void testValueOf() throws Exception {
        final Long threeSecondsValueOfMilliseconds = threeSeconds.valueOf(TimeUnit.MILLISECONDS);

        assertEquals(threeThousandMilliseconds,
                     threeSecondsValueOfMilliseconds);
    }
}
