package com.siemens.cto.aem.service.state.jms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemaining;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JmsStateNotificationConsumerImplTest {

    private JmsStateNotificationConsumerImpl impl;
    private JmsPackage jmsPackage;
    private Stale stale;
    private MessageConsumer consumer;
    private Long staleTimePeriod;
    private TimeUnit staleTimeUnit;
    private TimeDuration pollDuration;

    @Before
    public void setup() {
        jmsPackage = mock(JmsPackage.class);
        staleTimePeriod = 10L;
        staleTimeUnit = TimeUnit.MINUTES;
        stale = new Stale(new TimeDuration(staleTimePeriod,
                                           staleTimeUnit));
        pollDuration = new TimeDuration(1L, TimeUnit.SECONDS);
        consumer = mock(MessageConsumer.class);
        when(jmsPackage.getConsumer()).thenReturn(consumer);
        impl = new JmsStateNotificationConsumerImpl(jmsPackage,
                                                    stale,
                                                    pollDuration);
    }

    @Test
    public void testClose() throws Exception {
        impl.close();
        verify(jmsPackage, times(1)).close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddNotification() {
        final CurrentState<?, ?> unused = mock(CurrentState.class);
        impl.addNotification(unused);
    }

    @Test
    public void testGetNotifications() throws Exception {
        final int numberOfMessages = 5;
        configureMessageConsumerForMessages(numberOfMessages);
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(numberOfMessages);
        final List<CurrentState<?, ?>> states = impl.getNotifications(timeRemainingCalculator);

        assertEquals(numberOfMessages,
                     states.size());

        final Set<Long> expectedIds = new HashSet<>();
        for (int i = 0; i < numberOfMessages; i++) {
            expectedIds.add((long)(i+1));
        }
        for (final CurrentState state : states) {
            assertTrue(expectedIds.contains(state.getId().getId()));
        }
    }

    @Test
    public void testGetNotificationsWithJmsException() throws Exception {
        configureMessageConsumerForException();
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(1);
        final List<CurrentState<?, ?>> actualIds = impl.getNotifications(timeRemainingCalculator);

        assertTrue(actualIds.isEmpty());
    }

    @Test
    public void testReceiveReturnsNoMessage() throws Exception {
        when(consumer.receive(anyLong())).thenReturn(null);
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(1);
        final List<CurrentState<?, ?>> actualIds = impl.getNotifications(timeRemainingCalculator);

        assertTrue(actualIds.isEmpty());
        verify(timeRemainingCalculator, never()).getTimeRemaining();
    }

    protected void configureMessageConsumerForMessages(final int aNumberOfMessages) throws JMSException {
        final Message[] allMessages = createMockMessages(aNumberOfMessages);
        final Message[] restOfMessages = Arrays.copyOfRange(allMessages, 1, aNumberOfMessages);
        when(consumer.receive(anyLong())).thenReturn(allMessages[0], restOfMessages);
    }

    protected void configureMessageConsumerForException() throws JMSException {
        when(consumer.receive(anyLong())).thenThrow(new JMSException("Intentional failure for a test"));
    }

    protected Message[] createMockMessages(final int aNumberOfMessages) throws JMSException {
        final Message[] messages = new Message[aNumberOfMessages];
        for (int i = 0; i < aNumberOfMessages; i++) {
            messages[i] = createMapMessageMock(i + 1);
        }
        return messages;
    }

    protected MapMessage createMapMessageMock(final long anId) throws JMSException {
        final MapMessage message = mock(MapMessage.class);
        when(message.getString(eq(CommonStateKey.ID.getKey()))).thenReturn(String.valueOf(anId));
        when(message.getString(eq(CommonStateKey.TYPE.getKey()))).thenReturn(String.valueOf(StateType.JVM));
        when(message.getString(eq(CommonStateKey.AS_OF.getKey()))).thenReturn(ISODateTimeFormat.dateTime().print(DateTime.now()));
        when(message.getString(eq(CommonStateKey.STATE.getKey()))).thenReturn(JvmState.JVM_STARTED.toStateString());
        return message;
    }

    protected TimeRemainingCalculator createEnoughTimeForMessages(final int aNumberOfMessages) {
        final TimeRemainingCalculator calculator = mock(TimeRemainingCalculator.class);
        final int aNumberOfTimesRemaining = aNumberOfMessages;
        final TimeRemaining[] allTimeRemaining = createTimeRemaining(aNumberOfTimesRemaining);
        final TimeRemaining[] restOfTimeRemaining = Arrays.copyOfRange(allTimeRemaining, 1, aNumberOfTimesRemaining);
        when(calculator.getTimeRemaining()).thenReturn(allTimeRemaining[0],
                                                       restOfTimeRemaining);
        return calculator;
    }

    protected TimeRemaining[] createTimeRemaining(final int aNumberOfTimesRemaining) {
        final TimeRemaining[] remainingTimes = new TimeRemaining[aNumberOfTimesRemaining];
        for (int i = 0; i < aNumberOfTimesRemaining; i++) {
            final TimeRemaining time = new TimeRemaining(new TimeDuration((long)(aNumberOfTimesRemaining - i - 1),
                                                                          TimeUnit.SECONDS));
            remainingTimes[i] = time;
        }
        return remainingTimes;
    }
}
