package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StateConsumerManagerTest {

    private StateConsumerManager manager;
    private StateNotificationService stateNotificationService;
    private HttpServletRequest request;
    private HttpSession session;
    private StateNotificationConsumerId expectedId;
    private String clientId;
    private String sessionKey;

    @Before
    public void setUp() throws Exception {
        stateNotificationService = mock(StateNotificationService.class);
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        expectedId = mock(StateNotificationConsumerId.class);
        when(request.getSession()).thenReturn(session);
        manager = new StateConsumerManager(stateNotificationService);
        clientId = "123456";
        sessionKey = manager.createSessionKey(clientId);
    }

    @Test
    public void testGetConsumerIdFromScratch() throws Exception {
        when(stateNotificationService.register()).thenReturn(expectedId);

        final StateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                           clientId);

        assertEquals(expectedId,
                     actualId);
        verify(stateNotificationService, times(1)).register();
    }

    @Test
    public void testGetExistingValidConsumerId() throws Exception {
        when(session.getAttribute(eq(sessionKey))).thenReturn(expectedId);
        when(stateNotificationService.isValid(eq(expectedId))).thenReturn(true);

        final StateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                           clientId);

        assertEquals(expectedId,
                     actualId);
        verify(stateNotificationService, never()).register();
    }

    @Test
    public void testGetExistingInvalidConsumerId() throws Exception {
        final StateNotificationConsumerId newExpectedId = mock(StateNotificationConsumerId.class);
        when(session.getAttribute(eq(sessionKey))).thenReturn(expectedId);
        when(stateNotificationService.isValid(eq(expectedId))).thenReturn(false);
        when(stateNotificationService.register()).thenReturn(newExpectedId);

        final StateNotificationConsumerId actualId = manager.getConsumerId(request,
                                                                           clientId);

        assertEquals(newExpectedId,
                     actualId);
        verify(stateNotificationService, times(1)).register();
    }
}
