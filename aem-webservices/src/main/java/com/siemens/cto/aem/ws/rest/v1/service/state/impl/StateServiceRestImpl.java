package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.CurrentStateChronologicalComparator;
import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.ws.rest.v1.provider.TimeoutParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.state.StateServiceRest;

public class StateServiceRestImpl implements StateServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceRestImpl.class);
    private static final Comparator<CurrentState> REVERSE_CHRONOLOGICAL_ORDER = Collections.reverseOrder(CurrentStateChronologicalComparator.CHRONOLOGICAL);

    private final StateNotificationService stateNotificationService;
    private final StateConsumerManager stateConsumerManager;

    public StateServiceRestImpl(final StateNotificationService theStateNotificationService,
                                final StateConsumerManager theStateConsumerManager) {
        stateNotificationService = theStateNotificationService;
        stateConsumerManager = theStateConsumerManager;
    }

    @Override
    public Response pollJvmStates(final HttpServletRequest aRequest,
                                  final TimeoutParameterProvider aTimeoutParamProvider,
                                  final String aClientId) {
        LOGGER.debug("Poll states requested with timeout : {}", aTimeoutParamProvider);
        final StateNotificationConsumerId consumerId = stateConsumerManager.getConsumerId(aRequest,
                                                                                          aClientId);
        final List<CurrentState> updates = stateNotificationService.pollUpdatedStates(consumerId,
                                                                                      new TimeRemainingCalculator(aTimeoutParamProvider.valueOf()));
        final CurrentStateProcessor processor = new CurrentStateProcessor(updates,
                                                                          REVERSE_CHRONOLOGICAL_ORDER);
        final Collection<CurrentState> uniqueUpdates = processor.getUniqueStates();

        return ResponseBuilder.ok(uniqueUpdates);
    }
}
