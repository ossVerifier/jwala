package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.state.impl.InMemoryStateNotificationConsumerBuilderImpl;
import com.siemens.cto.aem.service.state.impl.InMemoryStateNotificationServiceImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by z0033r5b on 9/3/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerStateServiceImplTest {

    @Mock
    private StatePersistenceService<WebServer, WebServerReachableState> persistenceService;

    @Mock
    private GroupStateService.API groupStateService;

    @Mock
    private StateNotificationWorker stateNotificationWorker;

    @Mock
    private StateService<WebServer, WebServerReachableState> stateService;

    @Mock
    private GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;

    private StateNotificationService notificationService;

    private Identifier<WebServer> wsTestId = new Identifier<WebServer>(99L);

    @Before
    public void setUp() throws Exception {
        setupStateServiceMock();
        final StateNotificationConsumerBuilder notificationConsumerBuilder =
                new InMemoryStateNotificationConsumerBuilderImpl(new TimeDuration(5L, TimeUnit.MINUTES),
                new TimeDuration(30L, TimeUnit.SECONDS));
        notificationService = new InMemoryStateNotificationServiceImpl(notificationConsumerBuilder);
        stateService = new WebServerStateServiceImpl(persistenceService, notificationService, groupStateService,
                                                     stateNotificationWorker, grpStateComputationAndNotificationSvc);
    }

    @Test
    public void testCreateUnknown() {
        when(persistenceService.getState(wsTestId)).thenReturn(null);
        CurrentState<WebServer, WebServerReachableState> result = stateService.getCurrentState(wsTestId);
        assertEquals(WebServerReachableState.WS_UNKNOWN, result.getState());
    }

    private void setupStateServiceMock() {
        when(stateService.getCurrentState(Matchers.<Identifier<WebServer>>anyObject())).thenAnswer(new Answer<CurrentState<WebServer, WebServerReachableState>>() {
            @Override
            public CurrentState<WebServer, WebServerReachableState> answer(final InvocationOnMock invocation) throws Throwable {
                final Identifier<WebServer> webServer = (Identifier<WebServer>) invocation.getArguments()[0];
                return new CurrentState<>(webServer,
                        WebServerReachableState.WS_REACHABLE,
                        DateTime.now(),
                        StateType.WEB_SERVER);
            }
        });
    }

}
