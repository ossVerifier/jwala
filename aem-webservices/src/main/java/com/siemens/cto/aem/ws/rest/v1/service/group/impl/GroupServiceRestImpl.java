package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import javax.ws.rs.core.Response;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.ws.rest.v1.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;

public class GroupServiceRestImpl implements GroupServiceRest {

    private final GroupService groupService;

    public GroupServiceRestImpl(final GroupService theGroupService) {
        groupService = theGroupService;
    }

    @Override
    public Response getGroups(final PaginationParamProvider paginationParamProvider) {
        return ResponseBuilder.ok(groupService.getGroups(paginationParamProvider.getPaginationParameter()));
    }

    @Override
    public Response getGroup(final Identifier<Group> aGroupId) {
        return ResponseBuilder.ok(groupService.getGroup(aGroupId));
    }

//    @Override
    public Response createGroup(final String aNewGroupName) {
        //TODO We must put the user originating the request in here from however we get it
        return ResponseBuilder.ok(groupService.createGroup(aNewGroupName,
                                                           new User("hardCodedUser")));
    }
}
