package com.siemens.cto.aem.domain.model.webserver;

import com.siemens.cto.aem.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CreateWebServerRequestTest {

    private static final String HOST = "host";
    private static final String NAME = "name";
    private static final Path STATUS_PATH = new Path("/statusPath");
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");
    private static final FileSystemPath HTTP_CONFIG_FILE = new FileSystemPath("d:/some-dir/httpd.conf");
    private static final Integer portNumber = 10000;
    private static final Integer httpsPort = 20000;

    final List<Identifier<Group>> groupIds = new ArrayList<>();

    final Collection<Identifier<Group>> groupIdsFour = new ArrayList<>();

    final CreateWebServerRequest webServer =
            new CreateWebServerRequest(groupIds, NAME, HOST, portNumber, httpsPort, STATUS_PATH, HTTP_CONFIG_FILE,
                    SVR_ROOT, DOC_ROOT);
    final CreateWebServerRequest webServerTen =
            new CreateWebServerRequest(groupIdsFour, "otherName", HOST, portNumber, httpsPort, STATUS_PATH,
                    HTTP_CONFIG_FILE, SVR_ROOT, DOC_ROOT);

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
        webServerTen.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPath() {
        final CreateWebServerRequest invalidPath =
                new CreateWebServerRequest(groupIdsFour, "otherName", HOST, 0, 0, new Path("abc"),
                        new FileSystemPath(""), SVR_ROOT, DOC_ROOT);
        invalidPath.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidFileSystemPath() {
        final CreateWebServerRequest invalidPath =
                new CreateWebServerRequest(groupIdsFour, "otherName", HOST, 0, 0, new Path("/abc"),
                        new FileSystemPath("/some-dir/httpd.conf/"), SVR_ROOT, DOC_ROOT);
        invalidPath.validate();
    }
}
