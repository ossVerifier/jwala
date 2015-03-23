package com.siemens.cto.aem.domain.model.resource.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.EnumDeserializationRule;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.resource.ResourceInstanceParentRule;
import com.siemens.cto.aem.domain.model.state.StateType;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by z003e5zv on 3/13/2015.
 */
public class CreateResourceInstanceCommand implements Serializable, Command {
    private static final long serialVersionUID = 1L;

    private final String resourceTypeName;
    private final Long parentId;
    private final String parentType;
    private final Map<String, String> attributes;

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(
                new EnumDeserializationRule<StateType>(this.parentType, new StateType[]{StateType.GROUP, StateType.JVM}),
                new ResourceInstanceParentRule(this.parentId, this.parentType));
    }

    public CreateResourceInstanceCommand(String resourceTypeName, Long parentId, String parentType, Map<String, String> attributes) {
        this.resourceTypeName = resourceTypeName;
        this.parentId = parentId;
        this.parentType = parentType;
        this.attributes = attributes;
    }

    public String getParentType() {
        return parentType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }
}
