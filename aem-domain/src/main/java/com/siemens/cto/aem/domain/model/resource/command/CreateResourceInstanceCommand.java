package com.siemens.cto.aem.domain.model.resource.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by z003e5zv on 3/13/2015.
 */
public class CreateResourceInstanceCommand implements Serializable, Command {
    private static final long serialVersionUID = 1L;

    private final String resourceTypeName;
    private final String name;
    private final Long groupId;
    private final Map<String, String> attributes;

    @Override
    public void validateCommand() throws BadRequestException {

    }

    public CreateResourceInstanceCommand(String resourceTypeName, String name, Long groupId, Map<String, String> attributes) {
        this.name = name;
        this.resourceTypeName = resourceTypeName;
        this.groupId = groupId;
        this.attributes = attributes;
    }

    public String getName() {
        return this.name;
    }
    public Long getGroupId() {
        return this.groupId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }
}
