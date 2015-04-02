package com.siemens.cto.aem.domain.model.resource;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
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
    private final String friendlyName;
    private final String resourceTypeName;
    private final Map<String, String> attributes;
    private final LiteGroup group;

    public ResourceInstance(final Identifier<ResourceInstance> resourceInstanceId, final String friendlyName, final String resourceTypeName, final LiteGroup group, final Map<String, String> attributes) {
        this.resourceInstanceId = resourceInstanceId;
        this.friendlyName = friendlyName;
        this.resourceTypeName = resourceTypeName;
        this.group = group;
        this.attributes = attributes;

    }

    public Identifier<ResourceInstance> getResourceInstanceId() {
        return resourceInstanceId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public LiteGroup getGroup() {
        return this.group;
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
                .append(this.getGroup(), that.getGroup())
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getResourceInstanceId())
                .append(this.getResourceTypeName())
                .append(this.getGroup())
                .toHashCode();
    }

}

