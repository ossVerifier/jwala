package com.siemens.cto.aem.web.controller;

import junit.framework.TestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IndexControllerTest extends TestCase {
    final IndexController ic = new IndexController();

    public void testIndex() {
        assertEquals("aem/index", ic.index());
    }

    public void testAbout() {
        assertEquals("aem/about", ic.about());
    }

    public void testSandbox() {
        assertEquals("aem/sandbox", ic.sandbox());
    }

    public void testScripts() {
        String result = ic.scripts("true", false);
        assertEquals("aem/dev-scripts", result);
        result = ic.scripts("true", true);
        assertEquals("aem/dev-scripts", result);
        result = ic.scripts("false", false);
        assertEquals("aem/prod-scripts", result);
        result = ic.scripts("false", true);
        assertEquals("aem/prod-scripts", result);
        result = ic.scripts(null, true);
        assertEquals("aem/dev-scripts", result);
        result = ic.scripts(null, false);
        assertEquals("aem/prod-scripts", result);
    }

    public void testDevModeTrue() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ModelAndView mv = ic.devMode("true", resp);
        verify(resp).addCookie(any(Cookie.class));
        assertNotNull(mv);
        assertEquals("{devMode=true}", mv.getModel().toString());
    }

    public void testDevModeFalse() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ModelAndView mv = ic.devMode("false", resp);
        verify(resp).addCookie(any(Cookie.class));
        assertNotNull(mv);
        assertEquals("{devMode=false}", mv.getModel().toString());
    }
}
