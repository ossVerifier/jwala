package com.siemens.cto.aem.control.webserver.impl;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.DefaultExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultWebServerExecRequestBuilderImplTest {

    private WebServer webServer;
    private DefaultExecCommandBuilderImpl impl;
    private String webServerName;

    @Before
    public void setup() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getCanonicalPath() + "/toc-control/src/test/resources");
        impl = new DefaultExecCommandBuilderImpl();
        webServer = mock(WebServer.class);
        webServerName = "theWebServerName";

        when(webServer.getName()).thenReturn(webServerName);
    }

    @Test
    public void testStart() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.START;

        impl.setEntityName(webServer.getName());
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build(new WindowsWebServerPlatformCommandProvider());
        final ExecCommand expectedCommand =
                new ShellCommand("`/usr/bin/cygpath /cygdrive/d/stp/siemens/lib/scripts/start-service.sh`",
                        "\"" + webServerName + "\"", "20");

        assertEquals(expectedCommand.toCommandString(), actualCommand.toCommandString());
    }

    @Test
    public void testStop() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.STOP;

        impl.setEntityName(webServer.getName());
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build(new WindowsWebServerPlatformCommandProvider());
        final ShellCommand expectedCommand =
                new ShellCommand("`/usr/bin/cygpath /cygdrive/d/stp/siemens/lib/scripts/stop-service.sh`",
                        "\"" + webServerName + "\"", "20");

        assertEquals(expectedCommand.toCommandString(), actualCommand.toCommandString());
    }

}
