package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.CurrentStateChronologicalComparator;
import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.ws.rest.v1.provider.TimeoutParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.state.StateServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.*;

public class StateServiceRestImpl implements StateServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceRestImpl.class);
    private static final Comparator<CurrentState<?, ?>> REVERSE_CHRONOLOGICAL_ORDER = Collections
            .reverseOrder(CurrentStateChronologicalComparator.CHRONOLOGICAL);

    private final StateNotificationService stateNotificationService;
    private final StateConsumerManager stateConsumerManager;
    private final Collection<CurrentState<?, ?>> noStates = Collections.emptyList();

    public StateServiceRestImpl(final StateNotificationService theStateNotificationService,
            final StateConsumerManager theStateConsumerManager) {
        stateNotificationService = theStateNotificationService;
        stateConsumerManager = theStateConsumerManager;
    }

    @Override
    public Response pollStates(final HttpServletRequest aRequest,
            final TimeoutParameterProvider aTimeoutParamProvider, final String aClientId) {
        LOGGER.debug("Poll states requested with timeout (ms) : {}", aTimeoutParamProvider);
        final StateNotificationConsumerId consumerId = stateConsumerManager.getConsumerId(aRequest, aClientId);
        List<CurrentState<?, ?>> updates = null;
        try {
            updates =
                    stateNotificationService.pollUpdatedStates(consumerId, new TimeRemainingCalculator(
                            aTimeoutParamProvider.valueOf()));
        } catch (JMSException e) {
            LOGGER.warn("JMSException caught:", e);
        }
        final CurrentStateProcessor processor = new CurrentStateProcessor(updates, REVERSE_CHRONOLOGICAL_ORDER);
        final Collection<CurrentState<?, ?>> uniqueUpdates = processor.getUniqueStates();

        return ResponseBuilder.ok(uniqueUpdates);
    }

    @Override
    public Response pollState(final HttpServletRequest aRequest, final String aClientId) {
        LOGGER.debug("Poll single state requested.");
        CurrentState<?, ?> update = null;
        StateNotificationConsumerId consumerId = null;

        try {
            consumerId = stateConsumerManager.getConsumerId(aRequest, aClientId);

            update = stateNotificationService.pollUpdatedState(consumerId);
        } catch (Exception e) {
            LOGGER.error("Can't poll for state(s)!", e);
            final Throwable cause = e.getCause();
            String message = e.getMessage();
            message = cause != null && !cause.getMessage().isEmpty() ? message + ": " + cause.getMessage() : message;
            // TODO this should also be pushed to the history table
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         new FaultCodeException(AemFaultType.CANNOT_CONNECT, message));
        }

        Collection<CurrentState<?, ?>> updates;

        if (update != null) {
            updates = new ArrayList<>(1);
            updates.add(update);
        } else {
            updates = noStates;
        }

        return ResponseBuilder.ok(updates);
    }
}
