package com.siemens.cto.aem.service.state;

import java.util.List;

import javax.jms.JMSException;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;

public interface StateNotificationService {

    StateNotificationConsumerId register();

    void deregister(final StateNotificationConsumerId aConsumerId);

    boolean isValid(final StateNotificationConsumerId aConsumerId);

    void notifyStateUpdated(final CurrentState<?, ?> aNotification);

    List<CurrentState<?, ?>> pollUpdatedStates(final StateNotificationConsumerId aConsumerId,
            final TimeRemainingCalculator aTimeRemaining) throws JMSException;

    CurrentState<?, ?> pollUpdatedState(final StateNotificationConsumerId aConsumerId) throws JMSException;
}
