package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link WebServerCommandServiceImpl}.
 *
 * Created by z003bpej on 8/27/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerCommandServiceImplTest {

    @Mock
    private WebServerService webServerService;

    @Mock
    private CommandExecutor executor;

    @Mock
    private JschBuilder jschBuilder;

    @Mock
    private JSch jSch;

    @Mock
    private SshConfiguration sshConfig;

    @Mock
    private WebServer aWebServer;

    final private Identifier<WebServer> id = new Identifier("1");

    private WebServerCommandServiceImpl impl;

    @Before
    public void setup() throws JSchException, CommandFailureException {
        when(aWebServer.getName()).thenReturn("Apache2.2");
        when(aWebServer.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));
        when(webServerService.getWebServer(eq(id))).thenReturn(aWebServer);
        when(jschBuilder.build()).thenReturn(jSch);
        when(executor.execute(any(CommandProcessorBuilder.class)))
                .thenReturn(new ExecData(new ExecReturnCode(1), "The content of HTTPD Conf", ""));
        impl = new WebServerCommandServiceImpl(webServerService, executor, jschBuilder, sshConfig);
    }

    @Test
    public void testGetHttpdConf() throws CommandFailureException {
        final ExecData execData = impl.getHttpdConf(id);
        assertEquals(execData.getStandardOutput(), "The content of HTTPD Conf");
    }

}