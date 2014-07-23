package com.siemens.cto.aem.service.state.jms;

import java.util.EnumMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.service.jvm.state.jms.JmsPackage;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.jms.sender.CurrentStateExtractorMap;
import com.siemens.cto.aem.service.state.jms.sender.message.CurrentStateMessageExtractor;

public class StateTypeJmsStateNotificationConsumerImpl extends JmsStateNotificationConsumerImpl<CurrentState<?,?>> implements StateNotificationConsumer<CurrentState<?,?>> {

    private final Map<StateType, CurrentStateMessageExtractor<CurrentState<?,?>>> extractors;

    public StateTypeJmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                                     final Stale theStale,
                                                     final TimeDuration theDefaultPollDuration) {
        this(theJmsPackage,
             theStale,
             theDefaultPollDuration,
             CurrentStateExtractorMap.DEFAULT.getMap());
    }

    public StateTypeJmsStateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                                     final Stale theStale,
                                                     final TimeDuration theDefaultPollDuration,
                                                     final Map<StateType, CurrentStateMessageExtractor<CurrentState<?,?>>> theExtractors) {
        super(theJmsPackage,
              theStale,
              theDefaultPollDuration);
        extractors = new EnumMap<>(theExtractors);
    }

    @Override
    protected CurrentState<?,?> getNoMessageRead() {
        return null;
    }

    @Override
    protected CurrentState<?,?> extractFromMessage(final Message aMessage) throws JMSException {
        if (aMessage instanceof MapMessage) {
            return extractFromMessageHelper((MapMessage)aMessage);
        } else {
            throw new JMSException("Unsupported JMS message type :" + aMessage.getClass() + " with message : {" + aMessage.toString() + "}");
        }
    }

    CurrentState<?,?> extractFromMessageHelper(final MapMessage aMapMessage) throws JMSException {
        final String rawStateType = aMapMessage.getString(CommonStateKey.TYPE.getKey());
        try {
            final StateType stateType = StateType.valueOf(rawStateType);
            return extractors.get(stateType).extract(aMapMessage);
        } catch (final IllegalArgumentException e) {
            throw new JMSException("Unmapped State Type: " + rawStateType);
        }
    }
}
