package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

@Entity
@Table(name = "GRP_APP_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupAppConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupAppConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.UPDATE_GROUP_APP_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupAppConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.templateName = :templateName")
})
public class JpaGroupAppConfigTemplate {
    public static final String GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES = "getGroupAppTemplateResourceNames";
    public static final String GET_GROUP_APP_TEMPLATE_CONTENT = "getGroupAppTemplateContent";
    public static final String UPDATE_GROUP_APP_TEMPLATE_CONTENT = "updateGroupAppTemplateContent";

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
