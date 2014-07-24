package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;

public class InMemoryStateNotificationServiceImpl extends AbstractStateNotificationService implements StateNotificationService {

    public InMemoryStateNotificationServiceImpl(final StateNotificationConsumerBuilder theConsumerBuilder) {
        super(theConsumerBuilder);
    }

    @Override
    public void notifyStateUpdated(final CurrentState aNotification) {
        notifyRegisteredConsumers(aNotification);
    }
}
