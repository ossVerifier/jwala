package com.siemens.cto.aem.domain.model.resource.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by z003e5zv on 3/16/2015.
 */
public class UpdateResourceInstanceAttributesCommand implements Serializable, Command {
    private static final long serialVersionUID = 1L;

    private final Identifier<ResourceInstance> resourceInstanceId;

    private final Map<String, String> attributes;

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(
        );
    }
    public UpdateResourceInstanceAttributesCommand(Identifier<ResourceInstance> resourceInstanceId, Map<String, String> attributes) {
        this.resourceInstanceId = resourceInstanceId;
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Identifier<ResourceInstance> getResourceInstanceId() {
        return this.resourceInstanceId;
    }

}
