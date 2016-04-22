package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.CurrentStateChronologicalComparator;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.state.StateNotificationConsumerId;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.provider.TimeoutParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.state.StateServiceRest;
import org.joda.time.DateTime;
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
    private final JvmService jvmService;
    private final WebServerService webServerService;
    private final Collection<CurrentState<?, ?>> noStates = Collections.emptyList();
    private final JvmStateService jvmStateService;

    public StateServiceRestImpl(final StateNotificationService theStateNotificationService,
                                final StateConsumerManager theStateConsumerManager, final JvmService jvmService,
                                final JvmStateService jvmStateService, final WebServerService webServerService) {
        stateNotificationService = theStateNotificationService;
        stateConsumerManager = theStateConsumerManager;
        this.jvmService = jvmService;
        this.webServerService = webServerService;
        this.jvmStateService = jvmStateService;
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
        CurrentState<?, ?> update;
        StateNotificationConsumerId consumerId;

        Collection<Group> groupSet = Collections.EMPTY_SET;
        CurrentState<Group, GroupState> groupCurrentState = null;

        try {
            consumerId = stateConsumerManager.getConsumerId(aRequest, aClientId);
            update = stateNotificationService.pollUpdatedState(consumerId);

            if (update != null) {
                if (StateType.JVM.equals(update.getType())) {
                    final Jvm jvm = jvmService.getJvm((Identifier<Jvm>) update.getId());
                    groupSet = jvm.getGroups();

                } else if (StateType.WEB_SERVER.equals(update.getType())) {
                    final WebServer webServer = webServerService.getWebServer((Identifier<WebServer>) update.getId());
                    groupSet = webServer.getGroups();
                }
            }

            for (final Group group : groupSet) {
                final Long webServerCount = webServerService.getWebServerCount(group.getName());
                final Long webServerStartedCount = webServerService.getWebServerStartedCount(group.getName());
                final Long webServerStoppedCount = webServerService.getWebServerStoppedCount(group.getName());
                final Long jvmCount = jvmService.getJvmCount(group.getName());
                final Long jvmStartedCount = jvmService.getJvmStartedCount(group.getName());
                final Long jvmStoppedCount = jvmService.getJvmStoppedCount(group.getName());
                final Long jvmForciblyStoppedCount = jvmService.getJvmForciblyStoppedCount(group.getName());
                groupCurrentState = new CurrentState<>(group.getId(), GroupState.GRP_UNKNOWN, DateTime.now(),
                        StateType.GROUP, webServerCount, webServerStartedCount, webServerStoppedCount, jvmCount,
                        jvmStartedCount, jvmStoppedCount, jvmForciblyStoppedCount);
            }

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
            updates = new ArrayList<>(2);
            updates.add(groupCurrentState);
            updates.add(update);
        } else {
            updates = noStates;
        }

        return ResponseBuilder.ok(updates);
    }

    @Override
    public Response requestCurrentStatesRetrievalAndNotification(final String groupName) {
        return ResponseBuilder.ok(jvmStateService.requestCurrentStatesRetrievalAndNotification(groupName));
    }
}
