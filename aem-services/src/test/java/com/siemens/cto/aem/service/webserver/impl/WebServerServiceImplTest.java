package com.siemens.cto.aem.service.webserver.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;

/**
 * Created by z0031wps on 4/2/2014.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceImplTest {

    @Mock
    private WebServerDao wsDao;

    private WebServerServiceImpl wsService;

    @Mock
    private WebServer mockWebServer;
    
    private Group group;

    @Before
    public void setUp() {
        group = new Group(new Identifier<Group>(1L), "the-group-name");
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(mockWebServer.getName()).thenReturn("the-jvm-name");
        when(mockWebServer.getHost()).thenReturn("the-jvm-hostname");
        when(mockWebServer.getGroup()).thenReturn(group);

        wsService = new WebServerServiceImpl(wsDao);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        when(wsDao.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        final WebServer webServer= wsService.getWebServer(new Identifier<WebServer>(1L));
        assertEquals(new Identifier<WebServer>(1L), webServer.getId());
        assertEquals(group.getId(), webServer.getGroup().getId());
        assertEquals("the-jvm-name", webServer.getName());
        assertEquals("the-jvm-hostname", webServer.getHost());
    }

}
