package com.siemens.cto.aem.domain.model.webserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

import static org.junit.Assert.assertEquals;

public class CreateWebServerCommandTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final String STATUS_PATH = "/statusPath";
    private static final Integer portNumber = 10000;
    private static final Integer httpsPort = 20000;

    final List<Identifier<Group>> groupIds = new ArrayList<>();

    final Collection<Identifier<Group>> groupIdsFour = new ArrayList<>();

    final CreateWebServerCommand webServer = new CreateWebServerCommand(groupIds, NAME, HOST, portNumber, httpsPort, STATUS_PATH);
    final CreateWebServerCommand webServerTen = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, portNumber, httpsPort, STATUS_PATH);

    @Test
    public void testGetGroups() {
        assertEquals(0, webServer.getGroups().size());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, webServer.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, webServer.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(portNumber, webServer.getPort());
    }

    @Test
    public void testGetStatusPath() {
        assertEquals(STATUS_PATH, webServer.getStatusPath());
    }

    @Test
    public void testValidateCommand() {
        webServerTen.validateCommand();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPath() {
        final CreateWebServerCommand invalidPath = new CreateWebServerCommand(groupIdsFour, "otherName", HOST, 0, 0, "abc");
        invalidPath.validateCommand();
    }
}
