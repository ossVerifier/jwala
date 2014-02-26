package com.siemens.cto.aem.ws.rest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Z003BPEJ on 2/25/14.
 */
public class ApplicationResponseStatusTest {

    @Test
    public void testValueOf() {
        assertEquals(ApplicationResponseStatus.SUCCESS, ApplicationResponseStatus.valueOf("SUCCESS"));
        assertEquals(ApplicationResponseStatus.RECORD_NOT_FOUND, ApplicationResponseStatus.valueOf("RECORD_NOT_FOUND"));
    }

    @Test
    public void testCodes() {
        ApplicationResponseStatus [] values = ApplicationResponseStatus.values();
        for (ApplicationResponseStatus sts: values) {
            assertEquals(Integer.toString(sts.ordinal()), sts.getCode());
        }
    }

}
