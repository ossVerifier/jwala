package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;

public class JvmServiceRestImpl implements JvmServiceRest {

    private final Logger logger;

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;

    public JvmServiceRestImpl(final JvmService theJvmService,
                              final JvmControlService theJvmControlService) {
        logger = LoggerFactory.getLogger(JvmServiceRestImpl.class);
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
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
    public Response createJvm(final JsonCreateJvm aJvmToCreate) {
        logger.debug("Create JVM requested: {}", aJvmToCreate);
        final User hardCodedUser = User.getHardCodedUser();

        if (aJvmToCreate.areGroupsPresent()) {
            return ResponseBuilder.created(jvmService.createAndAssignJvm(aJvmToCreate.toCreateAndAddCommand(),
                                                                         hardCodedUser));
        } else {
            return ResponseBuilder.created(jvmService.createJvm(aJvmToCreate.toCreateJvmCommand(),
                                                                hardCodedUser));
        }
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate) {
        logger.debug("Update JVM requested: {}", aJvmToUpdate);
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmCommand(),
                                                       User.getHardCodedUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId) {
        logger.debug("Delete JVM requested: {}", aJvmId);
        jvmService.removeJvm(aJvmId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId,
                               final JsonControlJvm aJvmToControl) {
        logger.debug("Control JVM requested: {} {}", aJvmId, aJvmToControl);
        return ResponseBuilder.ok(jvmControlService.controlJvm(new ControlJvmCommand(aJvmId,
                                                                                     aJvmToControl.toControlOperation()),
                                                               User.getHardCodedUser())); //Or ResponseBuilder.created();
    }
}
