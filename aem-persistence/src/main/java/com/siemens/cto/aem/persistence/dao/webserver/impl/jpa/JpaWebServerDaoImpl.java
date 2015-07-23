package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class JpaWebServerDaoImpl implements WebServerDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    public JpaWebServerDaoImpl() {
    }

    @Override
    public WebServer createWebServer(final Event<CreateWebServerCommand> aWebServer) {

        try {
            final CreateWebServerCommand createWebServerCommand = aWebServer.getCommand();
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
                    + aWebServer.getCommand().getName(), eee);
        }
    }

    @Override
    public WebServer updateWebServer(final Event<UpdateWebServerCommand> aWebServerToUpdate) {

        try {
            final UpdateWebServerCommand updateWebServerCommand = aWebServerToUpdate.getCommand();
            final AuditEvent auditEvent = aWebServerToUpdate.getAuditEvent();
            final Identifier<WebServer> webServerId = updateWebServerCommand.getId();
            final JpaWebServer jpaWebServer = getJpaWebServer(webServerId);

            final Collection<Identifier<Group>> groupIds = updateWebServerCommand.getNewGroupIds();

            final ArrayList<JpaGroup> groups = new ArrayList<>(groupIds != null ? groupIds.size() : 0);

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
                    + aWebServerToUpdate.getCommand().getNewName(), eee);
        } 
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerFrom(getJpaWebServer(aWebServerId));
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

        final JpaWebServer WebServer = getJpaWebServer(aWebServerId);
        entityManager.remove(WebServer);
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

        final Query query =
            entityManager.createNamedQuery(JpaWebServer.FIND_WEB_SERVER_BY_GROUP_QUERY);

        query.setParameter("groupId", aGroup.getId());

        return webserversFrom(query.getResultList());
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
        final Query q = entityManager.createNamedQuery(JpaWebServer.FIND_APPLICATIONS_QUERY);
        q.setParameter(JpaWebServer.WEB_SERVER_PARAM_NAME, aWebServerName);

        final ArrayList<Application> apps = new ArrayList<>(q.getResultList().size());
        for(final JpaApplication jpa : (List<JpaApplication>) q.getResultList()) {
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
        final Query q = entityManager.createNamedQuery(JpaWebServer.FIND_JVMS_QUERY);
        q.setParameter("wsName", aWebServerName);

        final ArrayList<Jvm> jvms = new ArrayList<>(q.getResultList().size());
        for(final JpaJvm jpaJvm : (List<JpaJvm>) q.getResultList()) {
            jvms.add((new JpaJvmBuilder(jpaJvm)).build());
        }
        return jvms;
    }

}
