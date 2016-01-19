package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.*;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebServerCrudServiceImpl extends AbstractCrudServiceImpl<JpaWebServer> implements WebServerCrudService {

    public WebServerCrudServiceImpl() {
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
        try {
            final JpaWebServer jpaWebServer = new JpaWebServer();

            jpaWebServer.setName(webServer.getName());
            jpaWebServer.setHost(webServer.getHost());
            jpaWebServer.setPort(webServer.getPort());
            jpaWebServer.setHttpsPort(webServer.getHttpsPort());
            jpaWebServer.setStatusPath(webServer.getStatusPath().getPath());
            jpaWebServer.setHttpConfigFile(webServer.getHttpConfigFile().getPath());
            jpaWebServer.setSvrRoot(webServer.getSvrRoot().getPath());
            jpaWebServer.setDocRoot(webServer.getDocRoot().getPath());
            jpaWebServer.setCreateBy(createdBy);

            return webServerFrom(create(jpaWebServer));
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_NAME,
                    "Web server with name already exists: " + webServer.getName(),
                    eee);
        }

    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String createdBy) {
        final JpaWebServer jpaWebServer = findById(webServer.getId().getId());

        jpaWebServer.setName(webServer.getName());
        jpaWebServer.setHost(webServer.getHost());
        jpaWebServer.setPort(webServer.getPort());
        jpaWebServer.setHttpsPort(webServer.getHttpsPort());
        jpaWebServer.setStatusPath(webServer.getStatusPath().getPath());
        jpaWebServer.setHttpConfigFile(webServer.getHttpConfigFile().getPath());
        jpaWebServer.setSvrRoot(webServer.getSvrRoot().getPath());
        jpaWebServer.setDocRoot(webServer.getDocRoot().getPath());
        jpaWebServer.setCreateBy(createdBy);

        return webServerFrom(update(jpaWebServer));
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerFrom(findById(aWebServerId.getId()));
    }

    @Override
    public List<WebServer> getWebServers() {
        return webServersFrom(findAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WebServer> findWebServers(final String aWebServerNameFragment) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaWebServer g WHERE g.name LIKE :WebServerName");
        query.setParameter("WebServerName", "?" + aWebServerNameFragment + "?");

        return webServersFrom(query.getResultList());
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        remove(aWebServerId.getId());
    }

    protected List<WebServer> webServersFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> webservers = new ArrayList<>();

        for (final JpaWebServer jpaWebServer : someJpaWebServers) {
            webservers.add(webServerFrom(jpaWebServer));
        }

        return webservers;
    }

    protected WebServer webServerFrom(final JpaWebServer aJpaWebServer) {
        return new JpaWebServerBuilder(aJpaWebServer).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaWebServer j WHERE :groupId MEMBER OF j.groups.id");
        query.setParameter("groupId", aGroupId.getId());

        final List<JpaWebServer> webservers = query.getResultList();
        for (final JpaWebServer webserver : webservers) {
            remove(webserver);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WebServer> findWebServersBelongingTo(final Identifier<Group> aGroup) {
        final Query query = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP);
        query.setParameter("groupId", aGroup.getId());
        final JpaGroup group = (JpaGroup) query.getSingleResult();
        group.getWebServers().size();
        return webserversFrom(group.getWebServers()); // TODO: Verify if we need to sort web servers by name.
    }

    protected WebServer webserverFrom(final JpaWebServer aJpaWebServer) {

        final JpaWebServerBuilder builder = new JpaWebServerBuilder(aJpaWebServer);

        return builder.build();
    }

    protected List<WebServer> webserversFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> webservers = new ArrayList<>();

        for (final JpaWebServer webserver : someJpaWebServers) {
            webservers.add(webserverFrom(webserver));
        }

        return webservers;
    }

    @Override
    public List<Application> findApplications(final String aWebServerName) {
        // TODO: Use named query
        Query q = entityManager.createQuery("SELECT ws FROM JpaWebServer ws WHERE ws.name = :wsName");
        q.setParameter(JpaApplication.WEB_SERVER_NAME_PARAM, aWebServerName);
        final JpaWebServer webServer = (JpaWebServer) q.getSingleResult();
        final long size = webServer.getGroups().size();

        q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_WEB_SERVER_NAME);
        q.setParameter(JpaApplication.GROUP_LIST_PARAM, webServer.getGroups());

        final List<Application> apps = new ArrayList<>(q.getResultList().size());
        for (final JpaApplication jpa : (List<JpaApplication>) q.getResultList()) {
            apps.add(JpaAppBuilder.appFrom(jpa));
        }
        return apps;
    }

    @Override
    public WebServer findWebServerByName(final String aWebServerName) {
        final Query q = entityManager.createNamedQuery(JpaWebServer.FIND_WEB_SERVER_BY_QUERY);
        q.setParameter(JpaWebServer.WEB_SERVER_PARAM_NAME, aWebServerName);

        return webServerFrom((JpaWebServer) q.getSingleResult());
    }

    @Override
    public List<Jvm> findJvms(final String aWebServerName) {
        // TODO: Use named query
        Query q = entityManager.createQuery("SELECT ws FROM JpaWebServer ws WHERE ws.name = :wsName");
        q.setParameter(JpaApplication.WEB_SERVER_NAME_PARAM, aWebServerName);
        final JpaWebServer webServer = (JpaWebServer) q.getSingleResult();
        final long size = webServer.getGroups().size();

        q = entityManager.createNamedQuery(JpaWebServer.FIND_JVMS_QUERY);
        q.setParameter("groups", webServer.getGroups());

        final List<Jvm> jvms = new ArrayList<>(q.getResultList().size());
        for (final JpaJvm jpaJvm : (List<JpaJvm>) q.getResultList()) {
            jvms.add((new JpaJvmBuilder(jpaJvm)).build());
        }
        return jvms;
    }

    @Override
    public List<String> getResourceTemplateNames(final String webServerName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("webServerName", webServerName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(final String webServerName, final String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("webServerName", webServerName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new NonRetrievableResourceTemplateContentException(webServerName, resourceTemplateName, e);
        }
    }

    @Override
    public void populateWebServerConfig(List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting) {
        for (UploadWebServerTemplateRequest request : uploadWSTemplateCommands) {
            final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT);
            q.setParameter("webServerName", request.getWebServer().getName());
            q.setParameter("templateName", request.getConfFileName());
            List results = q.getResultList();
            if (overwriteExisting || results.isEmpty()) {
                uploadWebServerTemplate(request);
            }
        }
    }

    @Override
    public JpaWebServerConfigTemplate uploadWebserverConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest) {
        return uploadWebServerTemplate(uploadWebServerTemplateRequest);
    }

    private JpaWebServerConfigTemplate uploadWebServerTemplate(UploadWebServerTemplateRequest request) {
        final WebServer webServer = request.getWebServer();
        Identifier<WebServer> id = webServer.getId();
        final JpaWebServer jpaWebServer = findById(id.getId());

        InputStream inStream = request.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE);
        query.setParameter("webServerName", webServer.getName());
        query.setParameter("templateName", request.getConfFileName());
        List<JpaWebServerConfigTemplate> templates = query.getResultList();
        JpaWebServerConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaWebServerConfigTemplate();
            jpaConfigTemplate.setWebServer(jpaWebServer);
            jpaConfigTemplate.setTemplateName(request.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for web server [" + webServer.getName() + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

    @Override
    public void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.UPDATE_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("webServerName", wsName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        int numEntities = 0;
        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(wsName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(wsName, resourceTemplateName);
        }
    }

}
