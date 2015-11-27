package com.siemens.cto.aem.domain.command.resource;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.rule.StringLengthRule;

import java.io.Serializable;

/**
 * Created by z003e5zv on 3/30/2015.
 */
public class UpdateResourceInstanceNameCommand implements Serializable, Command {

    private final Identifier<ResourceInstance> resourceInstanceIdentifier;

    public String getName() {
        return name;
    }

    public Identifier<ResourceInstance> getResourceInstanceIdentifier() {
        return resourceInstanceIdentifier;
    }

    private final String name;

    public UpdateResourceInstanceNameCommand(final Identifier<ResourceInstance> resourceInstanceIdentifier, final String name) {
        this.resourceInstanceIdentifier = resourceInstanceIdentifier;
        this.name = name;
    }
    @Override
    public void validateCommand() throws BadRequestException {
        new StringLengthRule(0,200, this.name);
    }

}
