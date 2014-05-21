package com.siemens.cto.aem.control.webserver.impl;

import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultWebServerExecCommandBuilderImplTest {

    private WebServer webServer;
    private DefaultWebServerExecCommandBuilderImpl impl;
    private String webServerName;

    @Before
    public void setup() {
        impl = new DefaultWebServerExecCommandBuilderImpl();
        webServer = mock(WebServer.class);
        webServerName = "theWebServerName";

        when(webServer.getName()).thenReturn(webServerName);
    }

    @Test
    public void testStart() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.START;

        impl.setWebServer(webServer);
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build();
        final ExecCommand expectedCommand = new ExecCommand("net",
                                                            "start",
                                                            "\"" + webServerName + "\"");
        assertEquals(expectedCommand,
                     actualCommand);
    }

    @Test
    public void testStop() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.STOP;

        impl.setWebServer(webServer);
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build();
        final ExecCommand expectedCommand = new ExecCommand("net",
                                                            "stop",
                                                            "\"" + webServerName + "\"");
        assertEquals(expectedCommand,
                     actualCommand);
    }
}
