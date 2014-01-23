/*
 * Copyright(c) 2013 Siemens Medical Solutions Health
 * Services Corporation.  All rights reserved.  This software is
 * confidential, proprietary to Siemens, is protected by
 * copyright laws in the U.S. and abroad, and is licensed for use
 * by customers only in strict accordance with the license
 * agreement governing its use.
 */
package com.siemens.cto.aem.web.configuration.web;

import junit.framework.TestCase;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;

public class ApacheEnterpriseManagerWebConfigTest extends TestCase {

    private ApacheEnterpriseManagerWebConfig aemWC;

    @Override
    protected void setUp() {
        aemWC = new ApacheEnterpriseManagerWebConfig();
    }

    public void testConfigureContentNegotiationContentNegotiationConfigurer() {
        // This method will be tested in integration tests.
    }

    public void testAddResourceHandlersResourceHandlerRegistry() {
        // This method will be tested in integration tests.
    }

    public void testViewResolver() {
        final ViewResolver viewResolver = aemWC.viewResolver();
        assertNotNull(viewResolver);
    }

    public void testMessageSource() {
        final MessageSource messageSource = aemWC.messageSource();
        assertNotNull(messageSource);
    }

    public void testLocaleResolver() {
        final LocaleResolver localeResolver = aemWC.localeResolver();
        assertNotNull(localeResolver);
    }
}
