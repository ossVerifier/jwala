package com.siemens.cto.aem.request.resource;

import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by z003e5zv on 3/13/2015.
 */
public class ResourceInstanceRequest implements Serializable, Request {
    private static final long serialVersionUID = 1L;

    private final String resourceTypeName;
    private final String name;
    private final String groupName;
    private final Map<String, String> attributes;

    @Override
    public void validate() throws BadRequestException {

    }

    public ResourceInstanceRequest(String resourceTypeName, String name, String groupName, Map<String, String> attributes) {
        this.name = name;
        this.resourceTypeName = resourceTypeName;
        this.groupName = groupName;
        this.attributes = attributes;
    }

    public String getName() {
        return this.name;
    }
    public String getGroupName() {
        return this.groupName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }
}
