package com.siemens.cto.aem.service.state.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;
import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;

public abstract class AbstractStateNotificationService implements StateNotificationService {

    private final StateNotificationConsumerBuilder consumerBuilder;
    private final ConcurrentMap<StateNotificationConsumerId, StateNotificationConsumer> registeredConsumers;
    private final Lock pruneLock;

    protected AbstractStateNotificationService(final StateNotificationConsumerBuilder theConsumerBuilder) {
        this(theConsumerBuilder,
             Collections.<StateNotificationConsumerId, StateNotificationConsumer>emptyMap());
    }

    protected AbstractStateNotificationService(final StateNotificationConsumerBuilder theConsumerBuilder,
                                               final Map<StateNotificationConsumerId, StateNotificationConsumer> someConsumers) {
        consumerBuilder = theConsumerBuilder;
        registeredConsumers = new ConcurrentHashMap<>(someConsumers);
        pruneLock = new ReentrantLock();
    }

    @Override
    public StateNotificationConsumerId register() {
        final StateNotificationConsumer consumer = createConsumer();
        return registerConsumer(consumer);
    }

    @Override
    public void deregister(final StateNotificationConsumerId aConsumerId) {
        final StateNotificationConsumer consumer = registeredConsumers.remove(aConsumerId);
        if (consumer != null) {
            consumer.close();
        }
    }

    @Override
    public boolean isValid(final StateNotificationConsumerId aConsumerId) {
        final StateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);
        return (consumer != null) && (!consumer.isStale());
    }

    @Override
    public List<CurrentState> pollUpdatedStates(final StateNotificationConsumerId aConsumerId,
                                                                             final TimeRemainingCalculator aTimeRemaining) {
        final StateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);

        if (consumer != null) {
            final List<CurrentState> notifications = consumer.getNotifications(aTimeRemaining);
            return notifications;
        }

        return Collections.emptyList();
    }

    protected StateNotificationConsumer createConsumer() {
        return consumerBuilder.build();
    }

    protected StateNotificationConsumerId registerConsumer(final StateNotificationConsumer aConsumer) {
        final StateNotificationConsumerId id = new StateNotificationConsumerId();
        registeredConsumers.put(id, aConsumer);
        return id;
    }

    protected void notifyRegisteredConsumers(final CurrentState<Object, OperationalState> aNotification) {
        prune();
        for (final StateNotificationConsumer consumer : registeredConsumers.values()) {
            consumer.addNotification(aNotification);
        }
    }

    protected void prune() {
        if (pruneLock.tryLock()) {
            try {
                final Iterator<Map.Entry<StateNotificationConsumerId, StateNotificationConsumer>> candidates = registeredConsumers.entrySet().iterator();
                while (candidates.hasNext()) {
                    final Map.Entry<StateNotificationConsumerId, StateNotificationConsumer> candidate = candidates.next();
                    final StateNotificationConsumer candidateConsumer = candidate.getValue();
                    if (candidateConsumer.isStale()) {
                        candidates.remove();
                        candidateConsumer.close();
                    }
                }
            } finally {
                pruneLock.unlock();
            }
        }
    }
}
