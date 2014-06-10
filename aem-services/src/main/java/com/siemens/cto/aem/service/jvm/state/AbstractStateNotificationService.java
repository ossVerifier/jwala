package com.siemens.cto.aem.service.jvm.state;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public abstract class AbstractStateNotificationService implements JvmStateNotificationService {

    private final ConcurrentMap<JvmStateNotificationConsumerId, JvmStateNotificationConsumer> registeredConsumers;
    private final Lock pruneLock;

    protected AbstractStateNotificationService() {
        this(Collections.<JvmStateNotificationConsumerId, JvmStateNotificationConsumer>emptyMap());
    }

    protected AbstractStateNotificationService(final Map<JvmStateNotificationConsumerId, JvmStateNotificationConsumer> someConsumers) {
        registeredConsumers = new ConcurrentHashMap<>(someConsumers);
        pruneLock = new ReentrantLock();
    }

    @Override
    public JvmStateNotificationConsumerId register() {
        final JvmStateNotificationConsumer consumer = createConsumer();
        return registerConsumer(consumer);
    }

    @Override
    public void deregister(final JvmStateNotificationConsumerId aConsumerId) {
        final JvmStateNotificationConsumer consumer = registeredConsumers.remove(aConsumerId);
        if (consumer != null) {
            consumer.close();
        }
    }

    @Override
    public boolean isValid(final JvmStateNotificationConsumerId aConsumerId) {
        final JvmStateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);
        return (consumer != null) && (!consumer.isStale());
    }

    @Override
    public Set<Identifier<Jvm>> pollUpdatedStates(final JvmStateNotificationConsumerId aConsumerId,
                                                  final TimeRemainingCalculator aTimeRemaining) {
        final JvmStateNotificationConsumer consumer = registeredConsumers.get(aConsumerId);

        if (consumer != null) {
            final Set<Identifier<Jvm>> jvmIds = consumer.getNotifications(aTimeRemaining);
            return jvmIds;
        }

        return Collections.emptySet();
    }

    protected abstract JvmStateNotificationConsumer createConsumer();

    protected JvmStateNotificationConsumerId registerConsumer(final JvmStateNotificationConsumer aConsumer) {
        final JvmStateNotificationConsumerId id = new JvmStateNotificationConsumerId();
        registeredConsumers.put(id, aConsumer);
        return id;
    }

    protected void notifyRegisteredConsumers(final Identifier<Jvm> aJvmId) {
        prune();
        for (final JvmStateNotificationConsumer consumer : registeredConsumers.values()) {
            consumer.addNotification(aJvmId);
        }
    }

    protected void prune() {
        if (pruneLock.tryLock()) {
            try {
                final Iterator<Map.Entry<JvmStateNotificationConsumerId, JvmStateNotificationConsumer>> candidates = registeredConsumers.entrySet().iterator();
                while (candidates.hasNext()) {
                    final Map.Entry<JvmStateNotificationConsumerId, JvmStateNotificationConsumer> candidate = candidates.next();
                    final JvmStateNotificationConsumer candidateConsumer = candidate.getValue();
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
