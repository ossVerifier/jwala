package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.aem.domain.model.group.AddJvmsToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.ws.rest.v1.provider.LoggedOnUser;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;

public class GroupServiceRestImpl implements GroupServiceRest {

    private final Logger logger;
    private final GroupService groupService;
   
    @Autowired
    private GroupControlService groupControlService;

    public GroupServiceRestImpl(final GroupService theGroupService) {
        groupService = theGroupService;
        logger = LoggerFactory.getLogger(GroupServiceRestImpl.class);
    }

    @Override
    public Response getGroups(final PaginationParamProvider paginationParamProvider,
                              final NameSearchParameterProvider aGroupNameSearch) {
        final PaginationParameter pagination = paginationParamProvider.getPaginationParameter();
        logger.debug("Get Groups requested with pagination: {} and search: {}", pagination, aGroupNameSearch.getName());

        final List<Group> groups;
        if (aGroupNameSearch.isNamePresent()) {
            groups = groupService.findGroups(aGroupNameSearch.getName(),
                                             pagination);
        } else {
            groups = groupService.getGroups(pagination);
        }

        return ResponseBuilder.ok(groups);
    }

    @Override
    public Response getGroup(final Identifier<Group> aGroupId) {
        logger.debug("Get Group requested: {}", aGroupId);
        return ResponseBuilder.ok(groupService.getGroup(aGroupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName) {
        logger.debug("Create Group requested: {}", aNewGroupName);
        //TODO We must put the user originating the request in here from however we get it
        return ResponseBuilder.created(groupService.createGroup(new CreateGroupCommand(aNewGroupName),
                                                                User.getHardCodedUser()));
    }

    @Override
    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup) {
        logger.debug("Update Group requested: {}", anUpdatedGroup);
        //TODO We must put the user originating the request in here from however we get it
        return ResponseBuilder.ok(groupService.updateGroup(anUpdatedGroup.toUpdateGroupCommand(),
                                                           User.getHardCodedUser()));
    }

    @Override
    public Response removeGroup(final Identifier<Group> aGroupId) {
        logger.debug("Delete Group requested: {}", aGroupId);
        groupService.removeGroup(aGroupId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response removeJvmFromGroup(final Identifier<Group> aGroupId,
                                       final Identifier<Jvm> aJvmId) {
        logger.debug("Remove JVM from Group requested: {}, {}", aGroupId, aJvmId);
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupCommand(aGroupId,
                                                                                                aJvmId),
                                                                  User.getHardCodedUser()));
    }
    
    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd) {
        logger.debug("Add JVM to Group requested: {}, {}", aGroupId, someJvmsToAdd);
        final AddJvmsToGroupCommand command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                                                              User.getHardCodedUser()));
    }
    
    

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                   final JsonControlJvm aJvmControlOperation,
                                   final SecurityContext jaxrsSecurityContext) {
        logger.debug("Control all JVMs in Group requested: {}, {}", aGroupId, aJvmControlOperation);
        final JvmControlOperation command = aJvmControlOperation.toControlOperation();
        final ControlGroupCommand grpCommand = new ControlGroupCommand(aGroupId, 
                GroupControlOperation.convertFrom(command.getExternalValue()) );
        return ResponseBuilder.ok(
                groupControlService.controlGroup(grpCommand, LoggedOnUser.fromContext(jaxrsSecurityContext))
               );
    }

}
