package com.siemens.cto.aem.service.state;

import java.util.Set;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;

public interface StateNotificationService<S> {

    StateNotificationConsumerId<S> register();

    void deregister(final StateNotificationConsumerId<S> aConsumerId);

    boolean isValid(final StateNotificationConsumerId<S> aConsumerId);

    void notifyStateUpdated(final Identifier<S> aNotification);

    Set<Identifier<S>> pollUpdatedStates(final StateNotificationConsumerId<S> aConsumerId,
                                         final TimeRemainingCalculator aTimeRemaining);
}
