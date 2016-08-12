package com.cerner.jwala.common.request.resource;

import java.io.Serializable;
import java.util.Map;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceInstance;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;

/**
 * Created by z003e5zv on 3/16/2015.
 */
public class UpdateResourceInstanceAttributesRequest implements Serializable, Request {
    private static final long serialVersionUID = 1L;

    private final Identifier<ResourceInstance> resourceInstanceId;

    private final Map<String, String> attributes;

    @Override
    public void validate() {
        new MultipleRules(
        );
    }
    public UpdateResourceInstanceAttributesRequest(Identifier<ResourceInstance> resourceInstanceId, Map<String, String> attributes) {
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
