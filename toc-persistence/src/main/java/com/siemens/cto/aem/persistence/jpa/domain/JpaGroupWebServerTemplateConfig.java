package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

@Entity
@Table(name = "GRP_WEBSERVER_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
public class JpaGroupWebServerTemplateConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction=org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    @Column(name="TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name="TEMPLATE_CONTENT", nullable = false, length=2147483647)
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
        this.grp= jpaGroup;
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
