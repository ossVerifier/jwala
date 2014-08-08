package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;

public class JvmServiceRestImpl implements JvmServiceRest {

    private final Logger logger;

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;
    private final StateService<Jvm, JvmState> jvmStateService;

    public JvmServiceRestImpl(final JvmService theJvmService,
                              final JvmControlService theJvmControlService,
                              final StateService<Jvm, JvmState> theJvmStateService) {
        logger = LoggerFactory.getLogger(JvmServiceRestImpl.class);
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
        jvmStateService = theJvmStateService;
    }

    @Override
    public Response getJvms(final PaginationParamProvider paginationParamProvider) {
        logger.debug("Get JVMs requested with pagination: {}", paginationParamProvider);
        final List<Jvm> jvms = jvmService.getJvms(paginationParamProvider.getPaginationParameter());
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        logger.debug("Get JVM requested: {}", aJvmId);
        return ResponseBuilder.ok(jvmService.getJvm(aJvmId));
    }

    @Override
    public Response createJvm(final JsonCreateJvm aJvmToCreate,
                              final AuthenticatedUser aUser) {
        logger.debug("Create JVM requested: {}", aJvmToCreate);
        final User user = aUser.getUser();

        final Jvm jvm;
        if (aJvmToCreate.areGroupsPresent()) {
            jvm = jvmService.createAndAssignJvm(aJvmToCreate.toCreateAndAddCommand(),
                                                user);
        } else {
            jvm = jvmService.createJvm(aJvmToCreate.toCreateJvmCommand(),
                                       user);
        }
        return ResponseBuilder.created(jvm);
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate,
                              final AuthenticatedUser aUser) {
        logger.debug("Update JVM requested: {}", aJvmToUpdate);
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmCommand(),
                                                       aUser.getUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId) {
        //TODO This needs to be audited
        logger.debug("Delete JVM requested: {}", aJvmId);
        jvmService.removeJvm(aJvmId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId,
                               final JsonControlJvm aJvmToControl,
                               final AuthenticatedUser aUser) {
        logger.debug("Control JVM requested: {} {}", aJvmId, aJvmToControl);
        final JvmControlHistory controlHistory = jvmControlService.controlJvm(new ControlJvmCommand(aJvmId, aJvmToControl.toControlOperation()),
                                                                              aUser.getUser());
        final ExecData execData = controlHistory.getExecData();
        if (execData.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(controlHistory);
        } else {
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL,
                                             execData.getStandardError());
        }
    }

//    @Override
//    public Response pollJvmStates(final HttpServletRequest aRequest,
//                                  final TimeoutParameterProvider aTimeoutParamProvider,
//                                  final String aClientId) {
//        logger.debug("Poll JVM states requested with timeout : {}", aTimeoutParamProvider);
//        final JvmStateNotificationConsumerId consumerId = jvmStateConsumerManager.getConsumerId(aRequest,
//                                                                                                aClientId);
//        final Set<Identifier<Jvm>> updatedJvmIds = jvmStateNotificationService.pollUpdatedStates(consumerId,
//                                                                                                 new TimeRemainingCalculator(aTimeoutParamProvider.valueOf()));
//        final Set<CurrentJvmState> currentJvmStates = jvmStateService.getCurrentJvmStates(updatedJvmIds);
//        return ResponseBuilder.ok(currentJvmStates);
//    }

    @Override
    public Response getCurrentJvmStates(final JvmIdsParameterProvider aJvmIdsParameterProvider) {
        logger.debug("Current JVM states requested : {}", aJvmIdsParameterProvider);
        final Set<Identifier<Jvm>> jvmIds = aJvmIdsParameterProvider.valueOf();
        final Set<CurrentState<Jvm, JvmState>> currentJvmStates;

        if (jvmIds.isEmpty()) {
            currentJvmStates = jvmStateService.getCurrentStates(PaginationParameter.all());
        } else {
            currentJvmStates = jvmStateService.getCurrentStates(jvmIds);
        }

        return ResponseBuilder.ok(currentJvmStates);
    }
}
