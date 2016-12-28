package com.cerner.jwala.common.jsch.impl;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.JschServiceException;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.jcraft.jsch.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link JschServiceImpl}
 *
 * Created by Jedd Cuison on 12/26/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JschServiceImplTest.Config.class})
public class JschServiceImplTest {

    @Autowired
    private JschService jschService;

    @Mock
    private ChannelShell mockChannelShell;

    @Mock
    private ChannelExec mockChannelExec;

    @Mock
    private InputStream mockIn;

    @Mock
    private InputStream mockInErr;

    @Mock
    private OutputStream mockOut;

    @Mock
    private RemoteSystemConnection mockRemoteSystemConnection;

    @Mock
    private Session mockSession;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testRunCommandUsingChannelShell() throws IOException, JSchException {
        when(mockChannelShell.getInputStream()).thenReturn(new ByteArrayInputStream("EXIT_CODE=0*** \0xff".getBytes()));
        when(mockChannelShell.getOutputStream()).thenReturn(mockOut);
        final RemoteCommandReturnInfo result = jschService.runCommand("scp", mockChannelShell, 500);
        verify(mockOut, times(6)).write(any(byte[].class));
        verify(mockOut).flush();
        assertEquals("EXIT_CODE=0*** \0xff", result.standardOuput);
    }

    @Test(expected = JschServiceException.class)
    public void testRunCommandUsingChannelShellAndTimesOut() throws IOException, JSchException {
        when(mockChannelShell.getInputStream()).thenReturn(mockIn);
        when(mockChannelShell.getOutputStream()).thenReturn(mockOut);
        when(mockIn.available()).thenReturn(0);
        final RemoteCommandReturnInfo result = jschService.runCommand("scp", mockChannelShell, 500);
        verify(mockOut, times(6)).write(any(byte[].class));
        verify(mockOut).flush();
        assertEquals("", result.standardOuput);
    }

    @Test
    public void testRunCommandUsingChannelExec() throws IOException, JSchException {
        when(mockChannelExec.getInputStream()).thenReturn(mockIn);
        when(mockChannelExec.getExitStatus()).thenReturn(0);
        when(mockIn.available()).thenReturn(1);
        when(mockIn.read()).thenReturn(0xfd);
        jschService.runCommand("scp", mockChannelExec, 0);
        verify(mockChannelExec).setCommand(any(byte[].class));
        verify(mockChannelExec).connect(anyInt());
    }

    @Test
    public void testRunCommandUsingChannelExecWithExitStatusNotZero() throws IOException, JSchException {
        when(mockChannelExec.getInputStream()).thenReturn(mockIn);
        when(mockChannelExec.getErrStream()).thenReturn(mockInErr);
        when(mockChannelExec.getExitStatus()).thenReturn(1);
        when(mockIn.available()).thenReturn(1);
        when(mockIn.read()).thenReturn(0xfd);
        when(mockInErr.available()).thenReturn(1);
        when(mockInErr.read()).thenReturn(0xfd);
        jschService.runCommand("scp", mockChannelExec, 0);
        verify(mockChannelExec).setCommand(any(byte[].class));
        verify(mockChannelExec).connect(anyInt());
    }

    @Test
    public void testPrepareSession() throws Exception {
        when(Config.mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockRemoteSystemConnection.getEncryptedPassword()).thenReturn("#@#$%".toCharArray());
        jschService.prepareSession(mockRemoteSystemConnection);
        verify(mockRemoteSystemConnection).getUser();
        verify(mockRemoteSystemConnection).getEncryptedPassword();
        verify(mockRemoteSystemConnection).getPort();
        verify(mockSession).setPassword(anyString());
        verify(mockSession, times(2)).setConfig(anyString(), anyString());
    }

    @Configuration
    static class Config {

        public static final JSch mockJsch = mock(JSch.class);

        @Bean
        public JSch getMockJsch() {
            return mockJsch;
        }

        @Bean
        public JschService getJschService() {
            return new JschServiceImpl();
        }

    }

}