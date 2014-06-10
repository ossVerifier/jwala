package com.siemens.cto.aem.service.jvm.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.AbstractStateNotificationConsumerImpl;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;
import com.siemens.cto.aem.service.jvm.state.jms.sender.message.JvmStateUpdatedMessageExtractor;

public class JmsJvmStateNotificationConsumerImpl extends AbstractStateNotificationConsumerImpl implements JvmStateNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsJvmStateNotificationConsumerImpl.class);
    private static final Identifier<Jvm> NO_MESSAGE_READ = null;

    private final JmsPackage jmsPackage;

    public JmsJvmStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                               final Stale theStale) {
        this(theJmsPackage,
             theStale,
             System.currentTimeMillis());
    }

    JmsJvmStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                        final Stale theStale,
                                        final long theLastAccessTime) {
        super(theStale,
              theLastAccessTime);
        jmsPackage = theJmsPackage;
    }

    @Override
    public void addNotification(final Identifier<Jvm> aJvmId) {
        throw new UnsupportedOperationException("This method should not be called because the topic subscription handles notifications.");
    }

    @Override
    protected void closeHelper() {
        jmsPackage.close();
    }

    @Override
    protected Identifier<Jvm> getNotificationsHelper(final TimeDuration someTimeLeft) {
        try {
            return read(someTimeLeft.valueOf(TimeUnit.MILLISECONDS));
        } catch (final JMSException | RuntimeException e) {
            LOGGER.info("Exception occurred while consuming a JVM State Notification", e);
        }
        return null;
    }

    synchronized Identifier<Jvm> read(final long aTimeout) throws JMSException {
        final Message message = jmsPackage.getConsumer().receive(aTimeout);
        if (message != null) {
            return new JvmStateUpdatedMessageExtractor(message).extractId();
        }
        return NO_MESSAGE_READ;
    }
}
