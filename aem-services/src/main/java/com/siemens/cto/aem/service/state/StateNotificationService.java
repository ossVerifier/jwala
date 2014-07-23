package com.siemens.cto.aem.service.state;

import java.util.List;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.KeyValueStateProvider;

public interface StateNotificationService<S extends KeyValueStateProvider> {

    StateNotificationConsumerId<S> register();

    void deregister(final StateNotificationConsumerId<S> aConsumerId);

    boolean isValid(final StateNotificationConsumerId<S> aConsumerId);

    void notifyStateUpdated(final S aNotification);

    List<S> pollUpdatedStates(final StateNotificationConsumerId<S> aConsumerId,
                              final TimeRemainingCalculator aTimeRemaining);
}
