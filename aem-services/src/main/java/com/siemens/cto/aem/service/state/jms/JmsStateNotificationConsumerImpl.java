package com.siemens.cto.aem.service.state.jms;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.impl.AbstractStateNotificationConsumerImpl;
import com.siemens.cto.aem.service.state.jms.sender.CurrentStateExtractorMap;
import com.siemens.cto.aem.service.state.jms.sender.message.CurrentStateMessageExtractor;

public class JmsStateNotificationConsumerImpl extends AbstractStateNotificationConsumerImpl implements StateNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsStateNotificationConsumerImpl.class);

    private final JmsPackage jmsPackage;
    private final Map<StateType, CurrentStateMessageExtractor> extractors;

    public JmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                            final Stale theStale,
                                            final TimeDuration theDefaultPollDuration) {
        this(theJmsPackage,
             theStale,
             theDefaultPollDuration,
             System.currentTimeMillis(),
             CurrentStateExtractorMap.DEFAULT.getMap());
    }

    JmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                     final Stale theStale,
                                     final TimeDuration theDefaultPollDuration,
                                     final long theLastAccessTime,
                                     final Map<StateType, CurrentStateMessageExtractor> theExtractors) {
        super(theStale,
              theDefaultPollDuration,
              theLastAccessTime);
        jmsPackage = theJmsPackage;
        extractors = new EnumMap<>(theExtractors);
    }

    @Override
    public void addNotification(final CurrentState aNotification) {
        throw new UnsupportedOperationException("This method should not be called because the topic subscription handles notifications.");
    }

    @Override
    protected void closeHelper() {
        jmsPackage.close();
    }

    @Override
    protected CurrentState getNotificationsHelper(final TimeDuration someTimeLeft) {
        try {
            return read(someTimeLeft.valueOf(TimeUnit.MILLISECONDS));
        } catch (final JMSException | RuntimeException e) {
            LOGGER.info("Exception occurred while consuming a JVM State Notification", e);
        }
        return null;
    }

    protected CurrentState getNoMessageRead() {
        return null;
    }

    protected CurrentState extractFromMessage(final Message aMessage) throws JMSException {
        if (aMessage instanceof MapMessage) {
            return extractFromMessageHelper((MapMessage)aMessage);
        } else {
            throw new JMSException("Unsupported JMS message type :" + aMessage.getClass() + " with message : {" + aMessage.toString() + "}");
        }
    }

    CurrentState extractFromMessageHelper(final MapMessage aMapMessage) throws JMSException {
        final String rawStateType = aMapMessage.getString(CommonStateKey.TYPE.getKey());
        try {
            final StateType stateType = StateType.valueOf(rawStateType);
            return extractors.get(stateType).extract(aMapMessage);
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("Unmapped State Type", e);
            throw new JMSException("Unmapped State Type: " + rawStateType);
        }
    }

    synchronized CurrentState read(final long aTimeout) throws JMSException {
        final Message message = jmsPackage.getConsumer().receive(aTimeout);
        if (message != null) {
            return extractFromMessage(message);
        }
        return getNoMessageRead();
    }
}
