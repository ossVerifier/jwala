package com.siemens.cto.aem.service.jvm.state.jms;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemaining;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessageKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JmsJvmStateNotificationConsumerImplTest {

    private JmsJvmStateNotificationConsumerImpl impl;
    private JmsPackage jmsPackage;
    private Stale stale;
    private MessageConsumer consumer;
    private Long staleTimePeriod;
    private TimeUnit staleTimeUnit;

    @Before
    public void setup() {
        jmsPackage = mock(JmsPackage.class);
        staleTimePeriod = 10L;
        staleTimeUnit = TimeUnit.MINUTES;
        stale = new Stale(new TimeDuration(staleTimePeriod,
                                           staleTimeUnit));
        consumer = mock(MessageConsumer.class);
        when(jmsPackage.getConsumer()).thenReturn(consumer);
        impl = new JmsJvmStateNotificationConsumerImpl(jmsPackage,
                                                       stale);
    }

    @Test
    public void testClose() throws Exception {
        impl.close();
        verify(jmsPackage, times(1)).close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddNotification() {
        impl.addNotification(new Identifier<Jvm>(123456L));
    }

    @Test
    public void testGetNotifications() throws Exception {
        final int numberOfMessages = 5;
        configureMessageConsumerForMessages(numberOfMessages);
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(numberOfMessages);
        final Set<Identifier<Jvm>> actualIds = impl.getNotifications(timeRemainingCalculator);

        assertEquals(numberOfMessages,
                     actualIds.size());
        for (int i = 0; i < numberOfMessages; i++) {
            assertTrue(actualIds.contains(new Identifier<Jvm>((long)(i + 1))));
        }
    }

    @Test
    public void testGetNotificationsWithJmsException() throws Exception {
        configureMessageConsumerForException();
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(1);
        final Set<Identifier<Jvm>> actualIds = impl.getNotifications(timeRemainingCalculator);

        assertTrue(actualIds.isEmpty());
    }

    @Test
    public void testReceiveReturnsNoMessage() throws Exception {
        when(consumer.receive(anyLong())).thenReturn(null);
        final TimeRemainingCalculator timeRemainingCalculator = createEnoughTimeForMessages(1);
        final Set<Identifier<Jvm>> actualIds = impl.getNotifications(timeRemainingCalculator);

        assertTrue(actualIds.isEmpty());
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
        when(message.getString(eq(JvmStateMessageKey.JVM_ID.getKey()))).thenReturn(String.valueOf(anId));
        return message;
    }

    protected TimeRemainingCalculator createEnoughTimeForMessages(final int aNumberOfMessages) {
        final TimeRemainingCalculator calculator = mock(TimeRemainingCalculator.class);
        final int aNumberOfTimesRemaining = aNumberOfMessages + 1;
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
