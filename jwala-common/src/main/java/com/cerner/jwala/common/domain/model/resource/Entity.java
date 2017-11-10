package com.cerner.jwala.common.domain.model.resource;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Resource entity that wraps type, group and target
 * <p>
 * Created by Jedd Cuison on 3/30/2016
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {
    private final String type;
    private final String group;
    private final String target;
    private final String parentName;

    @JsonCreator
    public Entity(@JsonProperty("type") final String type,
                  @JsonProperty("group") final String group,
                  @JsonProperty("target") final String target,
                  @JsonProperty("parentName") final String parentName) {
        this.type = type;
        this.group = group;
        this.target = target;
        this.parentName = parentName;
    }

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getTarget() {
        return target;
    }

    public String getParentName() {
        return parentName;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "type='" + type + '\'' +
                ", group='" + group + '\'' +
                ", target='" + target + '\'' +
                ", parentName='" + parentName + '\'' +
                '}';
    }
}
