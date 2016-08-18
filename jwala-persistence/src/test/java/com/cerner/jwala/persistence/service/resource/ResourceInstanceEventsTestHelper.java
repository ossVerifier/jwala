package com.cerner.jwala.persistence.service.resource;

import java.util.Map;

import com.cerner.jwala.common.request.resource.ResourceInstanceRequest;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class ResourceInstanceEventsTestHelper {

    public static ResourceInstanceRequest createEventWithResourceInstanceRequest(final String resourceTypeName, String name, final String groupName, final Map<String, String> attributes, final String userName) {
       return new ResourceInstanceRequest(resourceTypeName, name, groupName, attributes);
    }

}
