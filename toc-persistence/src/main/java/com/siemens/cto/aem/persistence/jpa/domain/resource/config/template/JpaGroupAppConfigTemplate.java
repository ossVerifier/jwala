package com.siemens.cto.aem.persistence.jpa.domain.resource.config.template;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about a group of application related resource configuration templates.
 */
@Entity
@Table(name = "GRP_APP_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupAppConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupAppConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.UPDATE_GROUP_APP_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupAppConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.QUERY_DELETE_GRP_APP_TEMPLATE, query = "DELETE FROM JpaGroupAppConfigTemplate t WHERE t.templateName = :templateName")
})
public class JpaGroupAppConfigTemplate extends ConfigTemplate {
    public static final String GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES = "getGroupAppTemplateResourceNames";
    public static final String GET_GROUP_APP_TEMPLATE_CONTENT = "getGroupAppTemplateContent";
    public static final String UPDATE_GROUP_APP_TEMPLATE_CONTENT = "updateGroupAppTemplateContent";
    public static final String QUERY_DELETE_GRP_APP_TEMPLATE = "deleteGrpAppTemplate";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    public JpaGroup getJpaGroup() {
        return grp;
    }

    public void setJpaGroup(JpaGroup jpaGroup) {
        this.grp = jpaGroup;
    }
}
