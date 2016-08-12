package com.cerner.jwala.ws.rest.v1.service.state.impl;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.time.TimeDuration;
import com.cerner.jwala.common.time.TimeRemainingCalculator;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.state.StateNotificationConsumerId;
import com.cerner.jwala.service.state.StateNotificationService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.v1.provider.TimeoutParameterProvider;
import com.cerner.jwala.ws.rest.v1.service.state.impl.StateConsumerManager;
import com.cerner.jwala.ws.rest.v1.service.state.impl.StateServiceRestImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by z0033r5b on 10/26/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class StateServiceRestImplTest {

    private StateServiceRestImpl cut;

    @Mock
    private StateNotificationService stateNotificationService;

    @Mock
    private StateConsumerManager stateConsumerManager;

    @Mock
    private JvmService mockJvmService;

    @Mock
    private JvmStateService mockJvmStateService;

    @Mock
    private WebServerService mockWebServerService;

    @Before
    public void setup() {
        stateNotificationService = mock(StateNotificationService.class);
        stateConsumerManager = mock(StateConsumerManager.class);
        cut = new StateServiceRestImpl(stateNotificationService, stateConsumerManager, mockJvmService, mockJvmStateService,
                mockWebServerService);
    }

    @Test
    public void testPollStates() throws JMSException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TimeoutParameterProvider mockTimeoutProvider = mock(TimeoutParameterProvider.class);
        when(mockTimeoutProvider.valueOf()).thenReturn(new TimeDuration(1000L, TimeUnit.MILLISECONDS));
        when(stateConsumerManager.getConsumerId(any(HttpServletRequest.class), anyString())).thenReturn(new StateNotificationConsumerId());
        when(stateNotificationService.pollUpdatedStates(any(StateNotificationConsumerId.class), any(TimeRemainingCalculator.class))).thenReturn(new ArrayList());
        Response response = cut.pollStates(mockRequest, mockTimeoutProvider, "clientId");
        assertNotNull(response.getEntity());

        when(stateNotificationService.pollUpdatedStates(any(StateNotificationConsumerId.class), any(TimeRemainingCalculator.class))).thenThrow(new JMSException("Test JMS Exeception"));
        try {
            response = cut.pollStates(mockRequest, mockTimeoutProvider, "clientId");
            assertNotNull(response.getEntity());
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }

    }

    @Test
    public void testPollState() throws JMSException {
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(stateConsumerManager.getConsumerId(any(HttpServletRequest.class), anyString())).thenReturn(new StateNotificationConsumerId());
        when(stateNotificationService.pollUpdatedState(any(StateNotificationConsumerId.class))).thenReturn(new CurrentState(new Identifier(1L), JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));
        Response response = cut.pollState(mockHttpRequest, "clientId");
        assertNotNull(response.getEntity());

        when(stateNotificationService.pollUpdatedState(any(StateNotificationConsumerId.class))).thenReturn(null);
        response = cut.pollState(mockHttpRequest, "clientId");
        assertNotNull(response.getEntity());
    }
    
}
