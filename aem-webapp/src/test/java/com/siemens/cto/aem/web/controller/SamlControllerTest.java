package com.siemens.cto.aem.web.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Z003BPEJ on 6/18/14.
 */
public class SamlControllerTest {

    final SamlController controller = new SamlController();

    @Test
    public void testIdProvider() {
        assertEquals("saml/idp", controller.idProvider());
    }

    @Test
    public void testIdProviderPost() {
        assertEquals("saml/post", controller.idProviderPost());
    }

}
