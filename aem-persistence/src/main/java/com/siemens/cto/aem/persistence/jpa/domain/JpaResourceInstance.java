package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.domain.model.resource.ResourceInstance;

import javax.persistence.*;
import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
@Entity
@Table(name = "RESOURCE_INSTANCE", uniqueConstraints = {@UniqueConstraint(columnNames = {"RESOURCE_INSTANCE_ID", "NAME", "GROUP_ID"})})
@NamedQueries({@NamedQuery(name=JpaResourceInstance.DELETE_RESOURCES_QUERY, query="DELETE FROM JpaResourceInstance resource WHERE resource.group.name = :groupName and resource.name IN :resourceNames")})
public class JpaResourceInstance extends AbstractEntity<JpaResourceInstance, ResourceInstance> {

    public final static String DELETE_RESOURCES_QUERY = "deleteResourcesQuery";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESOURCE_INSTANCE_ID")
    private Long resourceInstanceId;

    private String name;

    @Column(name = "RESOURCE_TYPE_NAME")
    private String resourceTypeName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")
    private JpaGroup group;

    @ElementCollection
    @JoinTable(name="RESOURCE_INSTANCE_ATTRIBUTES", joinColumns = @JoinColumn(name = "RESOURCE_INSTANCE_ID", referencedColumnName = "RESOURCE_INSTANCE_ID"))
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

    public String getName() {
        return this.name;
    }
    public JpaGroup getGroup() {
        return group;
    }

    public void setGroup(JpaGroup group) {
        this.group = group;
    }

    public Long getResourceInstanceId() {
        return resourceInstanceId;
    }

    public void setResourceInstanceId(Long resourceInstanceId) {
        this.resourceInstanceId = resourceInstanceId;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }
    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
