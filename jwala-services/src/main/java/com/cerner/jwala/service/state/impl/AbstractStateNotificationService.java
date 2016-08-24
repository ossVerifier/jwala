package com.cerner.jwala.service.state.impl;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.common.time.TimeRemainingCalculator;
import com.cerner.jwala.service.state.StateNotificationConsumer;
import com.cerner.jwala.service.state.StateNotificationConsumerBuilder;
import com.cerner.jwala.service.state.StateNotificationConsumerId;
import com.cerner.jwala.service.state.StateNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractStateNotificationService implements StateNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStateNotificationService.class);
    private final StateNotificationConsumerBuilder consumerBuilder;
    private final ConcurrentMap<StateNotificationConsumerId, StateNotificationConsumer> registeredConsumers;
    private final Lock pruneLock;

    protected AbstractStateNotificationService(final StateNotificationConsumerBuilder theConsumerBuilder) {
        this(theConsumerBuilder, Collections.<StateNotificationConsumerId, StateNotificationConsumer> emptyMap());
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
    public boolean isValid(final StateNotificationConsumerId aConsumerId) {
        final StateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);
        return consumer != null && !consumer.isStale();
    }

    @Override
    public List<CurrentState<?, ?>> pollUpdatedStates(final StateNotificationConsumerId aConsumerId,
            final TimeRemainingCalculator aTimeRemaining) throws JMSException {
        final StateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);

        if (consumer != null) {
            final List<CurrentState<?, ?>> notifications = consumer.getNotifications(aTimeRemaining);
            return notifications;
        }

        return Collections.<CurrentState<?, ?>> emptyList();
    }

    @Override
    public CurrentState<?, ?> pollUpdatedState(final StateNotificationConsumerId aConsumerId) throws JMSException {

        final StateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);

        try {
            if (consumer != null) {
                return consumer.blockingGetNotification();
            }
        } catch (JMSException e) {
            LOGGER.debug("Error calling cached NotificationConsumer. Creating a new consumer for retry.", e);
            // Try to create a new consumer
            try {
                StateNotificationConsumer newConsumer = createConsumer();
                CurrentState<?, ?> ret = newConsumer.blockingGetNotification();
                // If we get here we did not throw an error on the
                // blockinGetNotification call, so stick this consumer
                // in the map.
                registeredConsumers.put(aConsumerId, newConsumer);
                return ret;
            } catch (JMSException ex) {
                LOGGER.error("Error creating new consumer. Rethrowing to caller...", ex);
                throw ex;
            }
        }
        return null;
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

    /**
     * Removes stale consumers.
     */
    protected void prune() {
        if (pruneLock.tryLock()) {
            try {
                final Iterator<Map.Entry<StateNotificationConsumerId, StateNotificationConsumer>> candidates =
                        registeredConsumers.entrySet().iterator();
                while (candidates.hasNext()) {
                    final Map.Entry<StateNotificationConsumerId, StateNotificationConsumer> candidate =
                            candidates.next();
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
