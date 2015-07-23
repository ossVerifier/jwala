package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;

import java.util.List;

public interface StateNotificationService {

    StateNotificationConsumerId register();

    void deregister(final StateNotificationConsumerId aConsumerId);

    boolean isValid(final StateNotificationConsumerId aConsumerId);

    void notifyStateUpdated(final CurrentState<?,?> aNotification);

    List<CurrentState<?, ?>> pollUpdatedStates(final StateNotificationConsumerId aConsumerId,
            final TimeRemainingCalculator aTimeRemaining);

    CurrentState<?, ?> pollUpdatedState(final StateNotificationConsumerId aConsumerId);
}
