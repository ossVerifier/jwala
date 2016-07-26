package com.siemens.cto.aem.ws.rest.v1.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.user.impl.UserServiceRestImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceRestTests {

    private MockMvc mvc;
    String authFlag;

    @Mock
    public UserServiceRestImpl impl;

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        authFlag = ApplicationProperties.get("toc.authorization");
    }

    @After
    public void tearDown() throws IOException {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testIsTOCAuthorizationEnabled() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(impl.isTOCAuthorizationEnabled(mockRequest, mockResponse))
                .thenReturn(ResponseBuilder.ok(UserServiceRestImpl.JSON_RESPONSE_FALSE));
        Response response = impl.isTOCAuthorizationEnabled(mockRequest, mockResponse);
        ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        Object content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_FALSE);
        System.setProperty("toc.authorization", "false");

        assertNotNull(response.getEntity());
        when(impl.isTOCAuthorizationEnabled(mockRequest, mockResponse))
                .thenReturn(ResponseBuilder.ok(UserServiceRestImpl.JSON_RESPONSE_TRUE));
        response = impl.isTOCAuthorizationEnabled(mockRequest, mockResponse);
        applicationResponse = (ApplicationResponse) response.getEntity();
        content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_TRUE);

    }

    @Test
    public void testIsUserAdmin() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(impl.isUserAdmin(mockRequest, mockResponse)).thenReturn(ResponseBuilder.ok(UserServiceRestImpl.JSON_RESPONSE_FALSE));
        Response response = impl.isUserAdmin(mockRequest, mockResponse);
        assertNotNull(response.getEntity());
        ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        Object content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_FALSE);

        when(impl.isUserAdmin(mockRequest, mockResponse)).thenReturn(ResponseBuilder.ok(UserServiceRestImpl.JSON_RESPONSE_TRUE));
        response = impl.isUserAdmin(mockRequest, mockResponse);
        assertNotNull(response.getEntity());
        applicationResponse = (ApplicationResponse) response.getEntity();
        content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_TRUE);
    }

}
