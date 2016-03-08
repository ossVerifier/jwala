package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

/**
 * Created by z003bpej on 8/25/2015.
 */
@Entity
@Table(name = "APP_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"APP_ID", "TEMPLATE_NAME", "JVM_ID"})})
@NamedQueries({
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT DISTINCT t.templateName FROM JpaApplicationConfigTemplate t WHERE t.app.name = :appName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaApplicationConfigTemplate t where t.app.name = :appName and t.templateName = :templateName and t.jvm = :templateJvm"),
        @NamedQuery(name = JpaApplicationConfigTemplate.UPDATE_APP_TEMPLATE_CONTENT,
                query = "UPDATE JpaApplicationConfigTemplate t SET t.templateContent = :templateContent WHERE t.app.name = :appName AND t.templateName = :templateName and t.jvm = :templateJvm"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE_NO_JVM,
                query = "SELECT t FROM JpaApplicationConfigTemplate t where t.templateName = :tempName and t.app.name = :appName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE,
                query = "SELECT t FROM JpaApplicationConfigTemplate t where t.templateName = :tempName and t.app.name = :appName and t.jvm.name = :jvmName")
})
public class JpaApplicationConfigTemplate {

    public static final String GET_APP_RESOURCE_TEMPLATE_NAMES = "getAppResourceTemplateNames";
    public static final String GET_APP_TEMPLATE_CONTENT = "getAppTemplateContent";
    public static final String UPDATE_APP_TEMPLATE_CONTENT = "updateAppTemplateContent";
    public static final String GET_APP_TEMPLATE_NO_JVM = "getAppTemplateNoJvm";
    public static final String GET_APP_TEMPLATE = "getAppTemplate";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaApplication app;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaJvm jvm;

    @Column(name = "TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name = "TEMPLATE_CONTENT", nullable = false, length = 2147483647)
    private String templateContent;

    public JpaApplication getApplication() {
        return app;
    }

    public void setApplication(final JpaApplication app) {
        this.app = app;
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

    public JpaJvm getJvm() {
        return jvm;
    }

    public void setJvm(JpaJvm jvm) {
        this.jvm = jvm;
    }
}
