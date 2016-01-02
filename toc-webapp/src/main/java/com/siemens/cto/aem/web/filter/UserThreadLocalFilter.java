package com.siemens.cto.aem.web.filter;

import com.siemens.cto.aem.common.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class UserThreadLocalFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserThreadLocalFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        User user = new User((String) httpServletRequest.getSession().getAttribute("user"));

        user.addToThread();
        filterChain.doFilter(servletRequest, servletResponse);
        user.invalidate();
    }


    @Override
    public void destroy() {
        //do nothing
    }
}
