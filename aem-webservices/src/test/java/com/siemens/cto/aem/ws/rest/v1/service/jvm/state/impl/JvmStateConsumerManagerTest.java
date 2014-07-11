package com.siemens.cto.aem.ws.rest.v1.service.jvm.state.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumerId;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmStateConsumerManagerTest {

    private JvmStateConsumerManager manager;
    private JvmStateNotificationService stateNotificationService;
    private HttpServletRequest request;
    private HttpSession session;
    private JvmStateNotificationConsumerId expectedId;
    private String clientId;
    private String sessionKey;

    @Before
    public void setUp() throws Exception {
        stateNotificationService = mock(JvmStateNotificationService.class);
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        expectedId = mock(JvmStateNotificationConsumerId.class);
        when(request.getSession()).thenReturn(session);
        manager = new JvmStateConsumerManager(stateNotificationService);
        clientId = "123456";
        sessionKey = manager.createSessionKey(clientId);
    }

    @Test
    public void testGetConsumerIdFromScratch() throws Exception {
        when(stateNotificationService.register()).thenReturn(expectedId);

        final JvmStateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                              clientId);

        assertEquals(expectedId,
                     actualId);
        verify(stateNotificationService, times(1)).register();
    }

    @Test
    public void testGetExistingValidConsumerId() throws Exception {
        when(session.getAttribute(eq(sessionKey))).thenReturn(expectedId);
        when(stateNotificationService.isValid(eq(expectedId))).thenReturn(true);

        final JvmStateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                              clientId);

        assertEquals(expectedId,
                     actualId);
        verify(stateNotificationService, never()).register();
    }

    @Test
    public void testGetExistingInvalidConsumerId() throws Exception {
        final JvmStateNotificationConsumerId newExpectedId = mock(JvmStateNotificationConsumerId.class);
        when(session.getAttribute(eq(sessionKey))).thenReturn(expectedId);
        when(stateNotificationService.isValid(eq(expectedId))).thenReturn(false);
        when(stateNotificationService.register()).thenReturn(newExpectedId);

        final JvmStateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                              clientId);

        assertEquals(newExpectedId,
                     actualId);
        verify(stateNotificationService, times(1)).register();
    }

}
