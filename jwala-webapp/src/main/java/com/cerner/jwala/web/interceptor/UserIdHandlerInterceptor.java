package com.cerner.jwala.web.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.cerner.jwala.common.UserId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts requests via Spring MVC interceptor which sets and un-sets the {@link UserId}.
 * TODO: This is a work in progress. This interceptor does not run when a REST request is made, this has to be fixed.
 *
 * Created by JC043760 on 12/30/2015.
 */
public class UserIdHandlerInterceptor implements HandlerInterceptor {

    public static final String USER = "user";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String userId = (String) request.getSession().getAttribute(USER);
        UserId.set(userId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserId.unset();
    }

}
