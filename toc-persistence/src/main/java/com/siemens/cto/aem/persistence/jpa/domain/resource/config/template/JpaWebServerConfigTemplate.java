package com.siemens.cto.aem.persistence.jpa.domain.resource.config.template;

import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about web server related resource configuration templates.
 *
 * Created by z003bpej on 8/25/2015.
 */
@Entity
@Table(name = "WEBSERVER_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"WEBSERVER_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT t.templateName FROM JpaWebServerConfigTemplate t WHERE t.webServer.name = :webServerName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaWebServerConfigTemplate t where t.webServer.name = :webServerName and t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_METADATA,
                query = "SELECT t.metaData FROM JpaWebServerConfigTemplate t where t.webServer.name = :webServerName and t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.UPDATE_WEBSERVER_TEMPLATE_CONTENT,
                query = "UPDATE JpaWebServerConfigTemplate t SET t.templateContent = :templateContent WHERE t.webServer.name = :webServerName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE,
        query = "SELECT t FROM JpaWebServerConfigTemplate t where t.webServer.name = :webServerName and t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.QUERY_DELETE_WEB_SERVER_TEMPLATE, query="DELETE FROM JpaWebServerConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.QUERY_DELETE_WEB_SERVER_TEMPLATE_BY_WEBSERVER_NAME, query="DELETE FROM JpaWebServerConfigTemplate t WHERE t.templateName = :templateName AND t.webServer.name = :webServerName"),
        @NamedQuery(name = JpaWebServerConfigTemplate.QUERY_GET_WEBSERVER_RESOURCE_TEMPLATES,
                query = "SELECT t FROM JpaWebServerConfigTemplate t WHERE t.webServer.name = :webServerName")
        })
public class JpaWebServerConfigTemplate extends ConfigTemplate {
    public static final String GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES = "getWebServerResourceTemplateNames";
    public static final String GET_WEBSERVER_TEMPLATE_CONTENT = "getWebServerTemplateContent";
    public static final String GET_WEBSERVER_TEMPLATE_METADATA = "getWebServerTemplateMetaData";
    public static final String UPDATE_WEBSERVER_TEMPLATE_CONTENT = "updateWebServerTemplateContent";
    public static final String GET_WEBSERVER_TEMPLATE = "getWebServerTemplate";
    public static final String QUERY_DELETE_WEB_SERVER_TEMPLATE = "deleteWebServerTemplate";
    public static final String QUERY_DELETE_WEB_SERVER_TEMPLATE_BY_WEBSERVER_NAME = "deleteWebServerTemplateByWebServerName";
    public static final String QUERY_GET_WEBSERVER_RESOURCE_TEMPLATES = "getWebServerResourceTemplates";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_WEBSERVER_NAME = "webServerName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)    
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction=org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaWebServer webServer;

    public JpaWebServer getWebServer() {
        return webServer;
    }

    public void setWebServer(final JpaWebServer webServer) {
        this.webServer = webServer;
    }
}
