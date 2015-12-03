package com.siemens.cto.aem.service.fault;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import org.junit.Test;
import org.springframework.integration.MessageHandlingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.siemens.cto.aem.service.fault.AemExceptionMapping}.
 *
 * Created by Z003BPEJ on 7/6/2015.
 */
public class AemExceptionMappingTest {

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsJSchExceptionTimedOut() {
        final Throwable penultimateRootCause = mock(JSchException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection timed out: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsJSchExceptionRefused() {
        final Throwable penultimateRootCause = mock(JSchException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection refused: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsResourceAccessExceptionTimedOut() {
        final Throwable penultimateRootCause = mock(ResourceAccessException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection timed out: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsResourceAccessExceptionRefused() {
        final Throwable penultimateRootCause = mock(ResourceAccessException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection refused: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsMessageHandlingException() {
        final Throwable penultimateRootCause = mock(MessageHandlingException.class);
        final Throwable httpClientErrorException = mock(HttpClientErrorException.class);
        when(penultimateRootCause.getCause()).thenReturn(httpClientErrorException);
        assertEquals(AemFaultType.INVALID_STATUS_PATH,
                     AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

}
