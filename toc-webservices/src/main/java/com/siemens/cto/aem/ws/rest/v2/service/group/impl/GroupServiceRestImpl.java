package com.siemens.cto.aem.ws.rest.v2.service.group.impl;

import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v2.service.group.GroupServiceRest;

import javax.ws.rs.core.Response;

/**
 * Implements {@link com.siemens.cto.aem.ws.rest.v2.service.group.GroupServiceRest}
 *
 * Created by JC043760 on 7/29/2016.
 */
public class GroupServiceRestImpl implements GroupServiceRest {

    @Override
    public Response getGroup(final String name) {
        // TODO: Actual implementation that retrieves the group
        return ResponseBuilder.ok("Group from Rest Service Version 2");
    }
}
