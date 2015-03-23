package com.siemens.cto.aem.domain.model.resource;

import com.siemens.cto.aem.domain.model.id.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * Created by z003e5zv on 3/13/2015.
 */
public class ResourceInstance {

    private static final long serialVersionUID = 1L;

    private final Identifier<ResourceInstance> resourceInstanceId;
    private final String resourceTypeName;
    private final Map<String, String> attributes;
    private final Long parentId;
    private final String parentType;

    public ResourceInstance(Identifier<ResourceInstance> resourceInstanceId, String resourceTypeName, Long parentId, String parentType, Map<String, String> attributes) {
        this.resourceInstanceId = resourceInstanceId;
        this.resourceTypeName = resourceTypeName;
        this.parentId = parentId;
        this.parentType = parentType;
        this.attributes = attributes;

    }

    public Identifier<ResourceInstance> getResourceInstanceId() {
        return resourceInstanceId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public String getParentType() {
        return this.parentType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ResourceInstance that = (ResourceInstance)obj;
        return new EqualsBuilder()
                .append(this.getResourceInstanceId(), that.getResourceInstanceId())
                .append(this.getResourceTypeName(), that.getResourceTypeName())
                .append(this.getParentId(), that.getParentId())
                .append(this.getParentType(), that.getParentType())
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getResourceInstanceId())
                .append(this.getResourceTypeName())
                .append(this.getParentId())
                .append(this.getParentType())
                .toHashCode();
    }

}

