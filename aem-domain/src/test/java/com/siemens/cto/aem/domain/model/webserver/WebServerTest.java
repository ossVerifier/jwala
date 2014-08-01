package com.siemens.cto.aem.domain.model.webserver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

import static org.junit.Assert.assertEquals;

public class WebServerTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final String STATUS_PATH = "/the status path";
    private static final Integer port = 10000;
    private static final Integer httpsPort = 20000;
    private static final Long id = 1L;
    private static final Identifier<WebServer> wsId = new Identifier<>(id);
    private final List<Group> groups = new ArrayList<>();
    private final WebServer ws = new WebServer(wsId, groups, NAME, HOST, port, httpsPort, STATUS_PATH);

    @Test
    public void testGetId() {
        assertEquals(wsId, ws.getId());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, ws.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, ws.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(port, ws.getPort());
    }

    @Test
    public void testGetGroups() {
        assertEquals(0, ws.getGroups().size());
    }

    @Test
    public void testGetGroupIds() {
        assertEquals(0, ws.getGroupIds().size());
    }

    @Test
    public void testStatusUri() throws Exception {
        final URI expectedUri = new URI("http",
                                null,
                                "my.expected.host.example.com",
                                12345,
                                STATUS_PATH,
                                null,
                                null);
        final WebServer webServer = new WebServer(new Identifier<WebServer>(123456L),
                                                  Collections.<Group>emptySet(),
                                                  "name",
                                                  expectedUri.getHost(),
                                                  expectedUri.getPort(),
                                                  99,
                                                  STATUS_PATH);
        final URI actualUri = webServer.getStatusUri();
        assertEquals(expectedUri,
                     actualUri);
    }
}
