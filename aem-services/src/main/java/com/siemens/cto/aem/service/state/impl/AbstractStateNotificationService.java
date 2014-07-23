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
import com.siemens.cto.aem.domain.model.state.KeyValueStateProvider;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;

public abstract class AbstractStateNotificationService<S extends KeyValueStateProvider> implements StateNotificationService<S> {

    private final ConcurrentMap<StateNotificationConsumerId<S>, StateNotificationConsumer<S>> registeredConsumers;
    private final Lock pruneLock;

    protected AbstractStateNotificationService() {
        this(Collections.<StateNotificationConsumerId<S>, StateNotificationConsumer<S>>emptyMap());
    }

    protected AbstractStateNotificationService(final Map<StateNotificationConsumerId<S>, StateNotificationConsumer<S>> someConsumers) {
        registeredConsumers = new ConcurrentHashMap<>(someConsumers);
        pruneLock = new ReentrantLock();
    }

    @Override
    public StateNotificationConsumerId<S> register() {
        final StateNotificationConsumer<S> consumer = createConsumer();
        return registerConsumer(consumer);
    }

    @Override
    public void deregister(final StateNotificationConsumerId<S> aConsumerId) {
        final StateNotificationConsumer<S> consumer = registeredConsumers.remove(aConsumerId);
        if (consumer != null) {
            consumer.close();
        }
    }

    @Override
    public boolean isValid(final StateNotificationConsumerId<S> aConsumerId) {
        final StateNotificationConsumer<S> consumer = registeredConsumers.get(aConsumerId);
        return (consumer != null) && (!consumer.isStale());
    }

    @Override
    public List<S> pollUpdatedStates(final StateNotificationConsumerId<S> aConsumerId,
                                     final TimeRemainingCalculator aTimeRemaining) {
        final StateNotificationConsumer<S> consumer = registeredConsumers.get(aConsumerId);

        if (consumer != null) {
            final List<S> notifications = consumer.getNotifications(aTimeRemaining);
            return notifications;
        }

        return Collections.emptyList();
    }

    protected abstract StateNotificationConsumer<S> createConsumer();

    protected StateNotificationConsumerId<S> registerConsumer(final StateNotificationConsumer<S> aConsumer) {
        final StateNotificationConsumerId<S> id = new StateNotificationConsumerId<>();
        registeredConsumers.put(id, aConsumer);
        return id;
    }

    protected void notifyRegisteredConsumers(final S aNotification) {
        prune();
        for (final StateNotificationConsumer<S> consumer : registeredConsumers.values()) {
            consumer.addNotification(aNotification);
        }
    }

    protected void prune() {
        if (pruneLock.tryLock()) {
            try {
                final Iterator<Map.Entry<StateNotificationConsumerId<S>, StateNotificationConsumer<S>>> candidates = registeredConsumers.entrySet().iterator();
                while (candidates.hasNext()) {
                    final Map.Entry<StateNotificationConsumerId<S>, StateNotificationConsumer<S>> candidate = candidates.next();
                    final StateNotificationConsumer<S> candidateConsumer = candidate.getValue();
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
