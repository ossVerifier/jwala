package com.cerner.jwala.service;

import com.cerner.jwala.service.impl.H2TcpServerServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created on 10/10/2016.
 */
public class H2ServerTest {

    private H2TcpServerServiceImpl service;

    @Before
    public void setUp() {
        service = new H2TcpServerServiceImpl(null);
    }

    @Test
    public void testStartServer() {
        service.startServer();
        assertTrue(service.isServerRunning());

        service.stopServer();
        assertFalse(service.isServerRunning());
    }

    @Test (expected = DbServerServiceException.class)
    public void testFailStartServer() {
        H2TcpServerServiceImpl badService = new H2TcpServerServiceImpl("-tcpPort,ERROR");
        badService.startServer();
    }
}


