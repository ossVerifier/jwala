package com.siemens.cto.aem.si;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { SshExecutingServiceTest.SshTestConfiguration.class })
public class SshExecutingServiceTest {

    private static final AtomicReference<SshConfiguration> SSH_CONFIGURATION_REFERENCE = new AtomicReference<>();
    private static final AtomicReference<JschBuilder> JSCH_BUILDER_REFERENCE = new AtomicReference<>();
    private static final AtomicReference<JSch> JSCH_REFERENCE = new AtomicReference<>();

    @Autowired
    private TestSshGateway gateway;

    @Autowired
    private TestSshServiceAdapter adapter;

    @Test
    public void testSuccessfulSshCommand() throws Exception {
        final String host = "this is the host";
        final String expectedCommand = "This is my Command";
        final Integer successfulExit = 0;
        final String sshInputData = "this is my command's output";
        final CommandOutput expectedExecData = new CommandOutput(new ExecReturnCode(successfulExit),
                                                       sshInputData,
                                                       "");

        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn(host);
        when(jvm.getJvmName()).thenReturn(expectedCommand);

        final SshConfiguration sshConfiguration = SSH_CONFIGURATION_REFERENCE.get();
        final String userName = sshConfiguration.getUserName();
        final Integer port = sshConfiguration.getPort();
        final Session session = mock(Session.class);
        final JSch jsch = JSCH_REFERENCE.get();
        when(jsch.getSession(eq(userName),
                             eq(host),
                             eq(port))).thenReturn(session);

        final InputStream sshInput = new ByteArrayInputStream(sshInputData.getBytes(StandardCharsets.UTF_8));
        final InputStream sshErr = new ByteArrayInputStream(new byte[]{});
        final OutputStream sshOut = new ByteArrayOutputStream();

        final ChannelExec channelExec = mock(ChannelExec.class);
        when(session.openChannel(eq("exec"))).thenReturn(channelExec);
        when(channelExec.getInputStream()).thenReturn(sshInput);
        when(channelExec.getErrStream()).thenReturn(sshErr);
        when(channelExec.getOutputStream()).thenReturn(sshOut);
        when(channelExec.getExitStatus()).thenReturn(successfulExit);

        final ArgumentCaptor<byte[]> commandCaptor = ArgumentCaptor.forClass(byte[].class);

        gateway.initiateRequest(jvm);

        verify(channelExec, times(1)).setCommand(commandCaptor.capture());
        final String actualCommand = new String(commandCaptor.getValue(), StandardCharsets.UTF_8);
        assertTrue(actualCommand.startsWith(expectedCommand));

        final CommandOutput actualExecData = adapter.getExecData();
        assertEquals(expectedExecData,
                     actualExecData);
    }

    @Test
    public void testFailedSshCommand() throws Exception {

        final Jvm expectedJvm = mock(Jvm.class);

        final JSch jsch = JSCH_REFERENCE.get();
        when(jsch.getSession(anyString(),
                             anyString(),
                             anyInt())).thenThrow(new JSchException());

        gateway.initiateRequest(expectedJvm);

        final Jvm actualJvm = adapter.getJvmInError();
        assertEquals(expectedJvm,
                     actualJvm);

    }

    @Configuration
    @ImportResource("classpath:/META-INF/test-ssh-integration.xml")
    static class SshTestConfiguration {

        @Bean
        public SshConfiguration getSshConfiguration() {
            final SshConfiguration config = mock(SshConfiguration.class);
            when(config.getUserName()).thenReturn("the User Name to use");
            when(config.getPort()).thenReturn(123456);
            SSH_CONFIGURATION_REFERENCE.set(config);
            return config;
        }

        @Bean
        public CommandExecutor getCommandExecutor() {
            final CommandExecutor executor = new ThreadedCommandExecutorImpl(Executors.newSingleThreadExecutor());
            return executor;
        }

        @Bean
        public JschBuilder getJschBuilder() throws JSchException {
            final JschBuilder builder = mock(JschBuilder.class);
            final JSch jsch = getJsch();
            when(builder.build()).thenReturn(jsch);
            JSCH_BUILDER_REFERENCE.set(builder);
            return builder;
        }

        private JSch getJsch() {
            final JSch jsch = mock(JSch.class);
            JSCH_REFERENCE.set(jsch);
            return jsch;
        }
    }
}
