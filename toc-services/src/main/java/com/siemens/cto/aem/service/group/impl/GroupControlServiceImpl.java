package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.request.group.ControlGroupRequest;
import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import org.springframework.transaction.annotation.Transactional;

public class GroupControlServiceImpl implements GroupControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupControlServiceImpl.class);
    private final GroupJvmControlService groupJvmControlService;
    private final GroupWebServerControlService groupWebServerControlService;
    
    public GroupControlServiceImpl(
            final GroupWebServerControlService theGroupWebServerControlService,
            final GroupJvmControlService theGroupJvmControlService) {
        groupWebServerControlService = theGroupWebServerControlService;
        groupJvmControlService = theGroupJvmControlService;
    }

    @Transactional
    @Override
    public void controlGroup(ControlGroupRequest controlGroupRequest, User aUser) {
        LOGGER.info("begin controlGroup operation {} for groupId {}", controlGroupRequest.getControlOperation(),
                controlGroupRequest.getGroupId());
        controlWebServers(controlGroupRequest, aUser);
        controlJvms(controlGroupRequest, aUser);
    }

    private void controlWebServers(ControlGroupRequest controlGroupRequest, User aUser) {

        WebServerControlOperation wsControlOperation = WebServerControlOperation.convertFrom(controlGroupRequest
                .getControlOperation().getExternalValue());

        ControlGroupWebServerRequest controlGroupWebServerCommand = new ControlGroupWebServerRequest(
                controlGroupRequest.getGroupId(), wsControlOperation);

        groupWebServerControlService.controlGroup(controlGroupWebServerCommand, aUser);
    }

    private void controlJvms(ControlGroupRequest controlGroupRequest, User aUser) {
  
        JvmControlOperation jvmControlOperation = JvmControlOperation.convertFrom(controlGroupRequest.getControlOperation()
                .getExternalValue());
        // TODO address this mapping between operations

        ControlGroupJvmRequest controlGroupJvmCommand = new ControlGroupJvmRequest(controlGroupRequest.getGroupId(),
                jvmControlOperation);

        groupJvmControlService.controlGroup(controlGroupJvmCommand, aUser);
    }

    @Override
    public void controlGroups(final ControlGroupRequest controlGroupRequest, final User user) {
        LOGGER.debug("Controlling groups. ControlGroupRequest = {}", controlGroupRequest);

        final WebServerControlOperation wsControlOperation = WebServerControlOperation
                .convertFrom(controlGroupRequest.getControlOperation().getExternalValue());
        groupWebServerControlService.controlAllWebSevers(new ControlGroupWebServerRequest(controlGroupRequest.getGroupId(),
                wsControlOperation), user);


        final JvmControlOperation jvmControlOperation = JvmControlOperation.convertFrom(controlGroupRequest.getControlOperation()
                .getExternalValue());

        groupJvmControlService.controlAllJvms(new ControlGroupJvmRequest(controlGroupRequest.getGroupId(), jvmControlOperation), user);
    }
}
