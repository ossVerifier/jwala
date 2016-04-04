package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

@Entity
@Table(name = "GRP_WEBSERVER_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupWebServerConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupWebServerConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupWebServerConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.QUERY_DELETE_GRP_WEBSERVER_TEMPLATE, query = "DELETE FROM JpaGroupWebServerConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.QUERY_DELETE_GROUP_WEBSERVER_TEMPLATE_BY_GROUP_NAME, query = "DELETE FROM JpaGroupWebServerConfigTemplate t WHERE t.templateName = :templateName AND t.jpaGroup.name = :groupName")
})
public class JpaGroupWebServerConfigTemplate {
    public static final String GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES = "getGroupWebServerTemplateResourcesName";
    public static final java.lang.String GET_GROUP_WEBSERVER_TEMPLATE_CONTENT = "getGroupWebServerTemplateContent";
    public static final java.lang.String UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT = "updateGroupWebServerTemplateContent";
    public static final String QUERY_DELETE_GRP_WEBSERVER_TEMPLATE = "deleteGrpWebServerTemplate";
    public static final String QUERY_DELETE_GROUP_WEBSERVER_TEMPLATE_BY_GROUP_NAME = "deleteGroupWebServerTemplateByGroupName";

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
