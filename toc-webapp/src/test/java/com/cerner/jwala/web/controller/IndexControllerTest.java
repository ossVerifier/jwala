package com.cerner.jwala.web.controller;

import junit.framework.TestCase;
import org.springframework.web.servlet.ModelAndView;

import com.cerner.jwala.web.controller.IndexController;

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

    public void testIndexPageScripts() {
        String result = ic.indexPageScripts("true", false);
        assertEquals("aem/dev-index-page-scripts", result);
        result = ic.indexPageScripts("true", true);
        assertEquals("aem/dev-index-page-scripts", result);
        result = ic.indexPageScripts("false", false);
        assertEquals("aem/prod-index-page-scripts", result);
        result = ic.indexPageScripts("false", true);
        assertEquals("aem/prod-index-page-scripts", result);
        result = ic.indexPageScripts(null, true);
        assertEquals("aem/dev-index-page-scripts", result);
        result = ic.indexPageScripts(null, false);
        assertEquals("aem/prod-index-page-scripts", result);
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

    public void testLogin() {
        assertEquals("aem/login", ic.login());
    }

    public void testLoginPageScripts() {
        String result = ic.loginPageScripts("true", false);
        assertEquals("aem/dev-login-page-scripts", result);
        result = ic.loginPageScripts("true", true);
        assertEquals("aem/dev-login-page-scripts", result);
        result = ic.loginPageScripts("false", false);
        assertEquals("aem/prod-login-page-scripts", result);
        result = ic.loginPageScripts("false", true);
        assertEquals("aem/prod-login-page-scripts", result);
        result = ic.loginPageScripts(null, true);
        assertEquals("aem/dev-login-page-scripts", result);
        result = ic.loginPageScripts(null, false);
        assertEquals("aem/prod-login-page-scripts", result);
    }
}
