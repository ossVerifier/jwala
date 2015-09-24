package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

/**
 * Created by z0033r5b on 8/18/2015.
 */
@Entity
@Table(name = "JVM_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"JVM_ID", "TEMPLATE_NAME"})})
@NamedQueries({
    @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT t.templateName FROM JpaJvmConfigTemplate t WHERE t.jvm.name = :jvmName"),
    @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaJvmConfigTemplate t where t.jvm.name = :jvmName and t.templateName = :templateName"),
    @NamedQuery(name = JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_CONTENT,
                query = "UPDATE JpaJvmConfigTemplate t SET t.templateContent = :templateContent WHERE t.jvm.name = :jvmName AND t.templateName = :templateName")
})
public class JpaJvmConfigTemplate {

    public static final String GET_JVM_RESOURCE_TEMPLATE_NAMES = "getJvmResourceTemplateNames";
    public static final String GET_JVM_TEMPLATE_CONTENT = "getJvmTemplateContent";
    public static final String UPDATE_JVM_TEMPLATE_CONTENT = "updateJvmTemplateContent";

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
