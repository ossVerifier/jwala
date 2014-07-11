package com.siemens.cto.aem.service.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.service.jvm.state.jms.JmsPackage;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.impl.AbstractStateNotificationConsumerImpl;

public abstract class JmsStateNotificationConsumerImpl<T> extends AbstractStateNotificationConsumerImpl<T> implements StateNotificationConsumer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsStateNotificationConsumerImpl.class);

    private final JmsPackage jmsPackage;

    public JmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                            final Stale theStale,
                                            final TimeDuration theDefaultPollDuration) {
        this(theJmsPackage,
             theStale,
             theDefaultPollDuration,
             System.currentTimeMillis());
    }

    JmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                     final Stale theStale,
                                     final TimeDuration theDefaultPollDuration,
                                     final long theLastAccessTime) {
        super(theStale,
              theDefaultPollDuration,
              theLastAccessTime);
        jmsPackage = theJmsPackage;
    }

    @Override
    public void addNotification(final T aNotification) {
        throw new UnsupportedOperationException("This method should not be called because the topic subscription handles notifications.");
    }

    @Override
    protected void closeHelper() {
        jmsPackage.close();
    }

    @Override
    protected T getNotificationsHelper(final TimeDuration someTimeLeft) {
        try {
            return read(someTimeLeft.valueOf(TimeUnit.MILLISECONDS));
        } catch (final JMSException | RuntimeException e) {
            LOGGER.info("Exception occurred while consuming a JVM State Notification", e);
        }
        return null;
    }

    protected abstract T getNoMessageRead();

    protected abstract T extractFromMessage(final Message aMessage) throws JMSException;

    synchronized T read(final long aTimeout) throws JMSException {
        final Message message = jmsPackage.getConsumer().receive(aTimeout);
        if (message != null) {
            return extractFromMessage(message);
        }
        return getNoMessageRead();
    }
}
