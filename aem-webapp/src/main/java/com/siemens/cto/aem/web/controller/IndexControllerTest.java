package com.siemens.cto.aem.web.controller;

import junit.framework.TestCase;

public class IndexControllerTest extends TestCase {

    public void testHello() {
        final IndexController ic = new IndexController();
        assertEquals("aem/Index", ic.hello());
    }
}
