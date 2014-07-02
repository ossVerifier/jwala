package com.siemens.cto.aem.service.jvm.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jms.core.JmsTemplate;

import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumerId;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JmsJvmStateNotificationServiceImplTest {

    private JmsJvmStateNotificationServiceImpl impl;
    private JvmStateService stateService;
    private JmsPackageBuilder builder;
    private JmsPackage jmsPackage;
    private JmsTemplate template;
    private Destination destination;
    private TimeDuration inactiveTime;
    private TimeDuration defaultPollTime;

    @Before
    public void setUp() throws Exception {
        setupStateService();
        setupBuilder();
        setupJmsTemplate();
        setupDestination();
        setupInactiveTime();
        setupDefaultPollTime();

        impl = spy(new JmsJvmStateNotificationServiceImpl(builder,
                                                          template,
                                                          destination,
                                                          inactiveTime,
                                                          defaultPollTime));
    }

    @Test
    public void testGetStateUpdates() throws Exception {

        final JvmStateNotificationConsumerId id = impl.register();

    }

    private void setupStateService() {
        stateService = mock(JvmStateService.class);
        when(stateService.getCurrentJvmState(Matchers.<Identifier<Jvm>>anyObject())).thenAnswer(new Answer<CurrentJvmState>() {
            @Override
            public CurrentJvmState answer(final InvocationOnMock invocation) throws Throwable {
                return new CurrentJvmState((Identifier<Jvm>)invocation.getArguments()[0],
                                           JvmState.STARTED,
                                           DateTime.now());
            }
        });
    }

    private void setupBuilder() {
        setupJmsPackage();
        builder = mock(JmsPackageBuilder.class);
        when(builder.build()).thenReturn(jmsPackage);
    }

    private void setupJmsPackage() {
        jmsPackage = mock(JmsPackage.class);
    }

    private void setupJmsTemplate() {
        template = mock(JmsTemplate.class);
    }

    private void setupDestination() {
        destination = mock(Destination.class);
    }

    private void setupInactiveTime() {
        inactiveTime = new TimeDuration(5L, TimeUnit.MINUTES);
    }

    private void setupDefaultPollTime() {
        inactiveTime = new TimeDuration(5L, TimeUnit.MINUTES);
    }
}
