package com.cerner.jwala.service.state.jms;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.service.state.StateNotificationConsumerBuilder;
import com.cerner.jwala.service.state.StateNotificationService;
import com.cerner.jwala.service.state.impl.AbstractStateNotificationService;
import com.cerner.jwala.service.state.jms.sender.message.MessageCreatorKeyValueStateConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Destination;

public class JmsStateNotificationServiceImpl extends AbstractStateNotificationService implements StateNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsStateNotificationServiceImpl.class);

    private final JmsTemplate template;
    private final Destination destination;

    public JmsStateNotificationServiceImpl(final JmsTemplate theTemplate,
                                           final Destination theDestination,
                                           final StateNotificationConsumerBuilder theConsumerBuilder) {
        super(theConsumerBuilder);
        template = theTemplate;
        destination = theDestination;
    }

    @SuppressWarnings("rawtypes")
    @Override
    @Transactional
    public void notifyStateUpdated(final CurrentState anUpdatedThing) {
        LOGGER.debug("Notifying state updated: {}", anUpdatedThing);
        prune();
        final MessageCreatorKeyValueStateConsumer consumer = new MessageCreatorKeyValueStateConsumer();
        anUpdatedThing.provideState(consumer); // puts state details in consumer
        template.send(destination, consumer);
    }

}
