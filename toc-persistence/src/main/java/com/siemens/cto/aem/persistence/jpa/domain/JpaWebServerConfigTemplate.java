package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;

/**
 * Created by z003bpej on 8/25/2015.
 */
@Entity
@Table(name = "WEBSERVER_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"WEBSERVER_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT t.templateName FROM JpaWebServerConfigTemplate t WHERE t.webServer.name = :webServerName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaWebServerConfigTemplate t where t.webServer.name = :webServerName and t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.UPDATE_WEBSERVER_TEMPLATE_CONTENT,
                query = "UPDATE JpaWebServerConfigTemplate t SET t.templateContent = :templateContent WHERE t.webServer.name = :webServerName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE,
        query = "SELECT t FROM JpaWebServerConfigTemplate t where t.webServer.name = :webServerName and t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.QUERY_DELETE_WEB_SERVER_TEMPLATE, query="DELETE FROM JpaWebServerConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.QUERY_DELETE_WEB_SERVER_TEMPLATE_BY_WEBSERVER_NAME, query="DELETE FROM JpaWebServerConfigTemplate t WHERE t.templateName = :templateName AND t.webServer.name = :webServerName")
        })
public class JpaWebServerConfigTemplate {

    public static final String GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES = "getWebServerResourceTemplateNames";
    public static final String GET_WEBSERVER_TEMPLATE_CONTENT = "getWebServerTemplateContent";
    public static final String UPDATE_WEBSERVER_TEMPLATE_CONTENT = "updateWebServerTemplateContent";
    public static final String GET_WEBSERVER_TEMPLATE = "getWebServerTemplate";
    public static final String QUERY_DELETE_WEB_SERVER_TEMPLATE = "deleteWebServerTemplate";
    public static final String QUERY_DELETE_WEB_SERVER_TEMPLATE_BY_WEBSERVER_NAME = "deleteWebServerTemplateByWebServerName";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_WEBSERVER_NAME = "webServerName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)    
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction=org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaWebServer webServer;

    @Column(name="TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name="TEMPLATE_CONTENT", nullable = false, length=2147483647)
    private String templateContent;

    public JpaWebServer getWebServer() {
        return webServer;
    }

    public void setWebServer(final JpaWebServer webServer) {
        this.webServer = webServer;
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
