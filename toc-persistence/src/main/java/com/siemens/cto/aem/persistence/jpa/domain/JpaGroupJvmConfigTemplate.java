package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

@Entity
@Table(name = "GRP_JVM_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupJvmConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupJvmConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupJvmConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_DELETE_GRP_JVM_TEMPLATE, query = "DELETE FROM JpaGroupJvmConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_JVM_TEMPLATE_BY_GROUP_NAME, query = "DELETE FROM JpaGroupJvmConfigTemplate t WHERE t.templateName = :templateName AND t.jpaGroup.name = :groupName")
})

public class JpaGroupJvmConfigTemplate {

    public static final String GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES = "getGroupJvmTemplateResourceNames";
    public static final String GET_GROUP_JVM_TEMPLATE_CONTENT = "getGroupJvmTemplateContent";
    public static final java.lang.String UPDATE_GROUP_JVM_TEMPLATE_CONTENT = "updateGroupJvmTemplateContent";
    public static final String QUERY_DELETE_GRP_JVM_TEMPLATE = "deleteGrpJvmTemplate";
    public static final String QUERY_DELETE_GROUP_JVM_TEMPLATE_BY_GROUP_NAME = "deleteGroupJvmTemplateByGroupName";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_GROUP_NAME = "groupName";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    @Column(name = "TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name = "TEMPLATE_CONTENT", nullable = false, length = 2147483647)
    private String templateContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JpaGroup getJpaGroup() {
        return grp;
    }

    public void setJpaGroup(JpaGroup jpaGroup) {
        this.grp = jpaGroup;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }
}
