package com.siemens.cto.aem.service.state.jms;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.state.KeyValueStateProvider;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.impl.AbstractStateNotificationService;
import com.siemens.cto.aem.service.state.jms.sender.message.MessageCreatorKeyValueStateConsumer;

public class JmsStateNotificationServiceImpl<S extends KeyValueStateProvider> extends AbstractStateNotificationService<S> implements StateNotificationService<S> {

    private final StateNotificationConsumerBuilder<S> consumerBuilder;
    private final JmsTemplate template;
    private final Destination destination;

    public JmsStateNotificationServiceImpl(final JmsTemplate theTemplate,
                                           final Destination theDestination,
                                           final StateNotificationConsumerBuilder<S> theConsumerBuilder) {
        super();
        template = theTemplate;
        destination = theDestination;
        consumerBuilder = theConsumerBuilder;
    }

    @Override
    @Transactional
    public void notifyStateUpdated(final S anUpdatedThing) {
        prune();
        final MessageCreatorKeyValueStateConsumer consumer = new MessageCreatorKeyValueStateConsumer();
        anUpdatedThing.provideState(consumer);
        template.send(destination,
                      consumer);
    }

    @Override
    protected StateNotificationConsumer<S> createConsumer() {
        return consumerBuilder.build();
    }
}
