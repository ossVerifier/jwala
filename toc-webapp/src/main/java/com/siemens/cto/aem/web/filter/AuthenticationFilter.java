package com.siemens.cto.aem.web.filter;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Authenticates request URL.
 * <p/>
 * Original Author: z002xuvs
 * Modified by: z003bpej
 * Comments: Modified to fit TOC, this filter was originally used in EPM.
 */
//@WebFilter(urlPatterns = "/*", asyncSupported = true)

public class AuthenticationFilter /*implements Filter */{

    private static final Pattern LOGIN_PAGE = Pattern.compile("/login");
    private static final Pattern GEN_PUBLIC_RESOURCES = Pattern.compile("/gen-public-resources");
    private static final Pattern PUBLIC_RESOURCES = Pattern.compile("/public-resources");

    private static final Pattern[] URL_EXCLUDES = {LOGIN_PAGE, GEN_PUBLIC_RESOURCES, PUBLIC_RESOURCES};

    private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class);

    //@Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    //@Override
    public void doFilter(ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpReq = (HttpServletRequest) request;
        final HttpServletResponse httpRes = (HttpServletResponse) response;

        if (!this.isExcludeUrl(httpReq)) {
            // This prevents the back button from displaying a secure page once logged out
            httpRes.setHeader("Cache-Control", "no-cache");
            httpRes.setHeader("Cache-Control", "no-store");
            httpRes.setDateHeader("Expires", 0);
            httpRes.setHeader("Pragma", "no-cache"); // HTTP 1.0 backward compatibility
        }

        AuthenticationFilter.LOG.debug("Verifying request " + httpReq.getRequestURI() + " requires validation.");
        if (!this.isExcludeUrl(httpReq) && !this.isAuthenticated(httpReq)) {
            AuthenticationFilter.LOG.debug("URL is restricted");
            httpRes.sendRedirect(httpReq.getContextPath() + "/login");
            return;
        } else {
            AuthenticationFilter.LOG.debug("URL is not restricted.");
        }

        if (this.isAuthenticated(httpReq)) {
            request = new UserRoleRequestWrapper((HttpServletRequest) request,
                    ((HttpServletRequest) request).getSession()
                            .getAttribute("user").toString());
        }

        chain.doFilter(request, response);
    }

    private boolean isAuthenticated(final HttpServletRequest httpReq) {
        return httpReq.getSession().getAttribute("user") != null;
    }

    //@Override
    public void destroy() {
    }

    private boolean isExcludeUrl(final HttpServletRequest request) {
        final String target = request.getRequestURI();
        for (final Pattern exclude : AuthenticationFilter.URL_EXCLUDES) {
            if (exclude.matcher(target).find()) {
                return true;
            }
        }
        return false;
    }

}