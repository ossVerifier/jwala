package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UpdateWebServerRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.dao.builder.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.domain.*;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.InputStream;
import java.util.*;

public class WebServerCrudServiceImpl implements WebServerCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    public WebServerCrudServiceImpl() {
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
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

        entityManager.persist(jpaWebServer);
        entityManager.flush();

        return webServerFrom(jpaWebServer);
    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String createdBy) {
        final JpaWebServer jpaWebServer = getJpaWebServer(webServer.getId());

        jpaWebServer.setName(webServer.getName());
        jpaWebServer.setHost(webServer.getHost());
        jpaWebServer.setPort(webServer.getPort());
        jpaWebServer.setHttpsPort(webServer.getHttpsPort());
        jpaWebServer.setStatusPath(webServer.getStatusPath().getPath());
        jpaWebServer.setHttpConfigFile(webServer.getHttpConfigFile().getPath());
        jpaWebServer.setSvrRoot(webServer.getSvrRoot().getPath());
        jpaWebServer.setDocRoot(webServer.getDocRoot().getPath());
        jpaWebServer.setCreateBy(createdBy);

        entityManager.flush();
        return webServerFrom(jpaWebServer);
    }

    @Override
    @Deprecated
    public WebServer createWebServer(final Event<CreateWebServerRequest> aWebServer) {

        try {
            final CreateWebServerRequest createWebServerCommand = aWebServer.getRequest();
            final AuditEvent auditEvent = aWebServer.getAuditEvent();
            final String userId = auditEvent.getUser().getUserId();
            final Calendar updateDate = auditEvent.getDateTime().getCalendar();
            final Collection<Identifier<Group>> groupIds = createWebServerCommand.getGroups();

            final List<JpaGroup> groups = new ArrayList<>(groupIds != null ? groupIds.size() : 0);

            if (groupIds != null) {
                for (final Identifier<Group> gid : groupIds) {
                    final JpaGroup group = getGroup(gid);
                    groups.add(group);
                }
            }

            final JpaWebServer jpaWebServer = new JpaWebServer();
            jpaWebServer.setName(createWebServerCommand.getName());
            jpaWebServer.setHost(createWebServerCommand.getHost());
            jpaWebServer.setPort(createWebServerCommand.getPort());
            jpaWebServer.setHttpsPort(createWebServerCommand.getHttpsPort());
            jpaWebServer.setGroups(groups);
            jpaWebServer.setStatusPath(createWebServerCommand.getStatusPath().getPath());
            jpaWebServer.setSvrRoot(createWebServerCommand.getSvrRoot().getPath());
            jpaWebServer.setDocRoot(createWebServerCommand.getDocRoot().getPath());
            jpaWebServer.setHttpConfigFile(createWebServerCommand.getHttpConfigFile().getPath());
            jpaWebServer.setCreateBy(userId);
            jpaWebServer.setCreateDate(updateDate);
            jpaWebServer.setUpdateBy(userId);
            jpaWebServer.setLastUpdateDate(updateDate);

            entityManager.persist(jpaWebServer);
            entityManager.flush();

            return webServerFrom(jpaWebServer);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_NAME, "WebServer Name already exists: "
                    + aWebServer.getRequest().getName(), eee);
        }
    }

    @Override
    @Deprecated
    public WebServer updateWebServer(final Event<UpdateWebServerRequest> aWebServerToUpdate) {

        try {
            final UpdateWebServerRequest updateWebServerCommand = aWebServerToUpdate.getRequest();
            final AuditEvent auditEvent = aWebServerToUpdate.getAuditEvent();
            final Identifier<WebServer> webServerId = updateWebServerCommand.getId();
            final JpaWebServer jpaWebServer = getJpaWebServer(webServerId);

            final Collection<Identifier<Group>> groupIds = updateWebServerCommand.getNewGroupIds();

            final List<JpaGroup> groups = new ArrayList<>(groupIds != null ? groupIds.size() : 0);

            if (groupIds != null) {
                for (final Identifier<Group> id : updateWebServerCommand.getNewGroupIds()) {
                    groups.add(getGroup(id));
                }
            }

            jpaWebServer.setName(updateWebServerCommand.getNewName());
            jpaWebServer.setPort(updateWebServerCommand.getNewPort());
            jpaWebServer.setHttpsPort(updateWebServerCommand.getNewHttpsPort());
            jpaWebServer.setHost(updateWebServerCommand.getNewHost());
            jpaWebServer.setGroups(groups);
            jpaWebServer.setStatusPath(updateWebServerCommand.getNewStatusPath().getPath());
            jpaWebServer.setHttpConfigFile(updateWebServerCommand.getNewHttpConfigFile().getPath());
            jpaWebServer.setSvrRoot(updateWebServerCommand.getNewSvrRoot().getPath());
            jpaWebServer.setDocRoot(updateWebServerCommand.getNewDocRoot().getPath());
            jpaWebServer.setUpdateBy(auditEvent.getUser().getUserId());
            jpaWebServer.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

            entityManager.flush();

            return webServerFrom(jpaWebServer);
        } catch (final PersistenceException eee) {
            // We have to catch the generalized exception because OpenJPA can throw a rollback instead.
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_NAME, "WebServer Name already exists: "
                    + aWebServerToUpdate.getRequest().getNewName(), eee);
        }
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerFrom(getJpaWebServer(aWebServerId));
    }

    @Override
    public JpaWebServer getJpaWebServer(long webServerId, boolean fetchGroups) {
        final JpaWebServer webServer = entityManager.find(JpaWebServer.class, webServerId);
        if (fetchGroups) {
            webServer.getGroups().size();
        }
        return webServer;
    }

    @Override
    public List<WebServer> getWebServers() {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaWebServer> criteria = builder.createQuery(JpaWebServer.class);
        final Root<JpaWebServer> root = criteria.from(JpaWebServer.class);

        criteria.select(root);

        final TypedQuery<JpaWebServer> query = entityManager.createQuery(criteria);

        return webServersFrom(query.getResultList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WebServer> findWebServers(final String aName) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaWebServer g WHERE g.name LIKE :WebServerName");
        query.setParameter("WebServerName", "?" + aName + "?");

        return webServersFrom(query.getResultList());
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {

        final JpaWebServer webServer = getJpaWebServer(aWebServerId);
        entityManager.remove(webServer);
    }

    protected List<WebServer> webServersFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> webservers = new ArrayList<>();

        for (final JpaWebServer jpaWebServer : someJpaWebServers) {
            webservers.add(webServerFrom(jpaWebServer));
        }

        return webservers;
    }

    protected JpaWebServer getJpaWebServer(final Identifier<WebServer> aWebServer) {

        final JpaWebServer jpaWebServer = entityManager.find(JpaWebServer.class, aWebServer.getId());

        if (jpaWebServer == null) {
            throw new NotFoundException(AemFaultType.WEBSERVER_NOT_FOUND, "WebServer not found: " + aWebServer);
        }

        return jpaWebServer;
    }

    protected WebServer webServerFrom(final JpaWebServer aJpaWebServer) {
        return new JpaWebServerBuilder(aJpaWebServer).build();
    }

    /**
     * TODO: DUPLICATED FROM JpaGroupDaoImpl - need some wiring internal to the Dao to reuse.
     *
     * @param aGroupId
     * @return
     */
    protected JpaGroup getGroup(final Identifier<Group> aGroupId) {

        if (aGroupId == null) {
            return null;
        }
        if (aGroupId.getId() == null) {
            return null;
        }

        final JpaGroup group = entityManager.find(JpaGroup.class, aGroupId.getId());

        if (group == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND, "Group not found: " + aGroupId);
        }

        return group;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaWebServer j WHERE j.group.id = :groupId");
        query.setParameter("groupId", aGroupId.getId());

        final List<JpaWebServer> webservers = query.getResultList();
        for (final JpaWebServer webserver : webservers) {
            entityManager.remove(webserver);
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
        for (UploadWebServerTemplateRequest command : uploadWSTemplateCommands) {
            final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT);
            q.setParameter("webServerName", command.getWebServer().getName());
            q.setParameter("templateName", command.getConfFileName());
            List results = q.getResultList();
            if (overwriteExisting || results.isEmpty()) {
                uploadWebServerTemplate(new Event<>(command, AuditEvent.now(user)));
            }
        }
    }

    @Override
    public JpaWebServerConfigTemplate uploadWebserverConfigTemplate(Event<UploadWebServerTemplateRequest> event) {
        return uploadWebServerTemplate(event);
    }

    private JpaWebServerConfigTemplate uploadWebServerTemplate(Event<UploadWebServerTemplateRequest> event) {
        final UploadWebServerTemplateRequest command = event.getRequest();
        final WebServer webServer = command.getWebServer();
        Identifier<WebServer> id = webServer.getId();
        final JpaWebServer jpaWebServer = getJpaWebServer(id);

        InputStream inStream = command.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE);
        query.setParameter("webServerName", webServer.getName());
        query.setParameter("templateName", command.getConfFileName());
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
            jpaConfigTemplate.setTemplateName(command.getConfFileName());
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

        try {
            if (q.executeUpdate() == 0) {
                throw new ResourceTemplateUpdateException(wsName, resourceTemplateName);
            }
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(wsName, resourceTemplateName, re);
        }
    }

}
