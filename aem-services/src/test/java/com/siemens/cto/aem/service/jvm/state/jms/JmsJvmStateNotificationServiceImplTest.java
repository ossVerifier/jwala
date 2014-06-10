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

    @Before
    public void setUp() throws Exception {
        setupStateService();
        setupBuilder();
        setupJmsTemplate();
        setupDestination();
        setupInactiveTime();

        impl = spy(new JmsJvmStateNotificationServiceImpl(
                builder,
                                                          template,
                                                          destination,
                                                          inactiveTime));
    }

//    @Test
//    public void testPruneWhenNotifyingJvmStateUpdated() throws Exception {
//        inactiveTime = new TimeDuration(10L, TimeUnit.MILLISECONDS);
//        impl = new JmsJvmStateNotificationServiceImpl(stateService,
//                                                      builder,
//                                                      template,
//                                                      destination,
//                                                      inactiveTime);
//        final int numberOfConsumers = 10;
//
//        for (int i = 0; i < numberOfConsumers; i++) {
//            impl.register();
//        }
//
//        final Collection<JvmStateNotificationConsumer> originalConsumers = impl.getCurrentConsumers();
//        for (final JvmStateNotificationConsumer consumer : originalConsumers) {
//            assertFalse(consumer.isClosed());
//        }
//
//        Thread.sleep(new TimeDuration(1L, TimeUnit.SECONDS).valueOf(TimeUnit.MILLISECONDS));
//
//        impl.notifyJvmStateUpdated(new Identifier<Jvm>(123456L));
//
//        assertTrue(impl.getCurrentConsumers().isEmpty());
//
//        for (final JvmStateNotificationConsumer consumer : originalConsumers) {
//            assertTrue(consumer.isClosed());
//        }
//    }

//    @Test
//    public void testRegister() throws Exception {
//        final ArgumentCaptor<JvmStateNotificationConsumer> consumerCaptor = ArgumentCaptor.forClass(JvmStateNotificationConsumer.class);
//        final int numberOfConsumers = 10;
//
//        for (int i = 0; i < numberOfConsumers; i++) {
//            final JvmStateNotificationConsumerId consumerId = impl.register();
//            assertNotNull(consumerId);
//        }
//
//        verify(impl, times(numberOfConsumers)).registerConsumer(consumerCaptor.capture());
//        verify(builder, times(numberOfConsumers)).build();
//
//        assertEquals(numberOfConsumers,
//                     impl.getCurrentConsumers().size());
//
//        for (final JvmStateNotificationConsumer consumer : impl.getCurrentConsumers()) {
//            assertTrue(consumerCaptor.getAllValues().contains(consumer));
//        }
//    }

//    @Test
//    public void testDeregister() throws Exception {
//        final int numberOfConsumers = 10;
//        final Set<JvmStateNotificationConsumerId> consumerIds = new HashSet<>(numberOfConsumers);
//
//        for (int i = 0; i < numberOfConsumers; i++) {
//            final JvmStateNotificationConsumerId consumerId = impl.register();
//            consumerIds.add(consumerId);
//        }
//
//        final Collection<JvmStateNotificationConsumer> originalConsumers = impl.getCurrentConsumers();
//
//        for (JvmStateNotificationConsumer consumer : originalConsumers) {
//            assertFalse(consumer.isClosed());
//        }
//
//        for (final JvmStateNotificationConsumerId consumerId : consumerIds) {
//            impl.deregister(consumerId);
//        }
//
//        for (JvmStateNotificationConsumer consumer : originalConsumers) {
//            assertTrue(consumer.isClosed());
//        }
//
//        assertTrue(impl.getCurrentConsumers().isEmpty());
//    }

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
}
