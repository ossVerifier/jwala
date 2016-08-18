package com.cerner.jwala.ws.rest.v1.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.user.impl.UserServiceRestImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceRestTests {

    String authFlag;
    public UserServiceRestImpl impl;
    String TOC_ROLE_ADMIN; 
    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        authFlag = ApplicationProperties.get("toc.authorization");
        impl = new UserServiceRestImpl();
        TOC_ROLE_ADMIN = ApplicationProperties.get("toc.role.admin");

    }

    @After
    public void tearDown() throws IOException {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }



    @Test
    public void testIsUserAdmin() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Response response = impl.isUserAdmin(mockRequest, mockResponse);
        assertNotNull(response.getEntity());
        ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        Object content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_FALSE);

    }

}
