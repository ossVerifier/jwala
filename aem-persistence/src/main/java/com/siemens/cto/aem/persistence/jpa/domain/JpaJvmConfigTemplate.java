package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

/**
 * Created by z0033r5b on 8/18/2015.
 */
@Entity
@Table(name = "JVM_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"JVM_ID", "TEMPLATE_NAME"})})
public class JpaJvmConfigTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private JpaJvm jvm;

    @Column(name="TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name="TEMPLATE_CONTENT", nullable = false)
    private String templateContent;

    public JpaJvm getJvm() {
        return jvm;
    }

    public void setJvm(final JpaJvm jvm) {
        this.jvm = jvm;
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

    public void setTemplateContent(final String templateContent) {
        this.templateContent = templateContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
