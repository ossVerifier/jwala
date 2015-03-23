package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.resource.ResourceInstance;

import javax.persistence.*;
import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
@Entity
@Table(name = "RESOURCE_INSTANCE", uniqueConstraints = {@UniqueConstraint(columnNames = {"RESOURCE_INSTANCE_ID"})})
public class JpaResourceInstance extends AbstractEntity<JpaResourceInstance, ResourceInstance> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESOURCE_INSTANCE_ID")
    private Long resourceInstanceId;

    @Column(name = "RESOURCE_TYPE_NAME")
    private String resourceTypeName;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "PARENT_TYPE")
    private String parentType;

    @ElementCollection
    @JoinTable(name="RESOURCE_INSTANCE_ATTRIBUTES", joinColumns = @JoinColumn(name = "RESOURCE_INSTANCE_ID"))
    @MapKeyColumn (name = "ATTRIBUTE_KEY")
    @Column(name = "ATTRIBUTE_VALUE")
    private Map<String, String> attributes;


    @Override
    public Long getId() {
        return resourceInstanceId;
    }

    public void setId(Long resourceInstanceId) {
        this.resourceInstanceId = resourceInstanceId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }
    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
