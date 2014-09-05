package com.siemens.cto.aem.common.exception;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class ExceptionUtilTest {

    private ExceptionUtil instance;

    @Before
    public void setup() {
        instance = ExceptionUtil.INSTANCE;
    }

    @Test
    public void testGetPenultimateRootCause() {

        final Exception e4 = new Exception("This is the child");
        final Exception e3 = new Exception("This is the parent", e4);
        final Exception e2 = new Exception("This is the grandparent", e3);
        final Exception e1 = new Exception("This is the great-grandparent", e2);

        final Throwable penultimate = instance.getPenultimateRootCause(e1);

        assertSame(e3,
                   penultimate);
    }

    @Test
    public void testWithNoUnderlyingCause() {

        final Exception e = new Exception("I am an orphan");

        final Throwable penultimate = instance.getPenultimateRootCause(e);

        assertSame(e,
                   penultimate);
    }
}
