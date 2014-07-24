package com.siemens.cto.aem.service.state.jms;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.impl.AbstractStateNotificationService;
import com.siemens.cto.aem.service.state.jms.sender.message.MessageCreatorKeyValueStateConsumer;

public class JmsStateNotificationServiceImpl extends AbstractStateNotificationService implements StateNotificationService {

    private final JmsTemplate template;
    private final Destination destination;

    public JmsStateNotificationServiceImpl(final JmsTemplate theTemplate,
                                           final Destination theDestination,
                                           final StateNotificationConsumerBuilder theConsumerBuilder) {
        super(theConsumerBuilder);
        template = theTemplate;
        destination = theDestination;
    }

    @Override
    @Transactional
    public void notifyStateUpdated(final CurrentState anUpdatedThing) {
        prune();
        final MessageCreatorKeyValueStateConsumer consumer = new MessageCreatorKeyValueStateConsumer();
        anUpdatedThing.provideState(consumer);
        template.send(destination,
                      consumer);
    }
}
