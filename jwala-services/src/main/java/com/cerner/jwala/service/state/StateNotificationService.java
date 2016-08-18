package com.cerner.jwala.service.state;

import java.util.List;

import javax.jms.JMSException;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.time.TimeRemainingCalculator;

public interface StateNotificationService {

    StateNotificationConsumerId register();

    boolean isValid(final StateNotificationConsumerId aConsumerId);

    void notifyStateUpdated(final CurrentState<?, ?> aNotification);

    List<CurrentState<?, ?>> pollUpdatedStates(final StateNotificationConsumerId aConsumerId,
            final TimeRemainingCalculator aTimeRemaining) throws JMSException;

    CurrentState<?, ?> pollUpdatedState(final StateNotificationConsumerId aConsumerId) throws JMSException;
}
