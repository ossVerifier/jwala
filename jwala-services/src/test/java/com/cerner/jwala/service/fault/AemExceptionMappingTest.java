package com.cerner.jwala.service.fault;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.service.fault.AemExceptionMapping;
import com.jcraft.jsch.JSchException;

import org.junit.Test;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.cerner.jwala.service.fault.AemExceptionMapping}.
 *
 * Created by Jedd Cuison on 7/6/2015.
 */
public class AemExceptionMappingTest {

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsJSchExceptionTimedOut() {
        final Throwable penultimateRootCause = mock(JSchException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection timed out: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.mapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsJSchExceptionRefused() {
        final Throwable penultimateRootCause = mock(JSchException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection refused: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.mapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsResourceAccessExceptionTimedOut() {
        final Throwable penultimateRootCause = mock(ResourceAccessException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection timed out: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.mapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

    @Test
    public void testMapGenericFaultTypesForRemoteConnectionsResourceAccessExceptionRefused() {
        final Throwable penultimateRootCause = mock(ResourceAccessException.class);
        final Throwable connectionException = mock(ConnectException.class);
        when(penultimateRootCause.getCause()).thenReturn(connectionException);
        when(connectionException.getMessage()).thenReturn("Connection refused: connect");
        assertEquals(AemFaultType.CANNOT_CONNECT,
                     AemExceptionMapping.mapGenericFaultTypesForRemoteConnections(penultimateRootCause));
    }

}
