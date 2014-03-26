package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.domain.JpaWebServer;

public class JpaWebServerDaoImpl implements WebServerDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public WebServer createWebServer(final Event<CreateWebServerCommand> aWebServer) {

        try {
            final CreateWebServerCommand createWebServerCommand = aWebServer.getCommand();
            final AuditEvent auditEvent = aWebServer.getAuditEvent();
            final String userId = auditEvent.getUser().getUserId();
            final Calendar updateDate = auditEvent.getDateTime().getCalendar();

            final JpaWebServer jpaWebServer = new JpaWebServer();
            jpaWebServer.setName(createWebServerCommand.getName());
            jpaWebServer.setHost(createWebServerCommand.getHost());
            jpaWebServer.setPort(createWebServerCommand.getPort());
            jpaWebServer.setCreateBy(userId);
            jpaWebServer.setCreateDate(updateDate);
            jpaWebServer.setUpdateBy(userId);
            jpaWebServer.setLastUpdateDate(updateDate);

            entityManager.persist(jpaWebServer);
            entityManager.flush();

            return webServerFrom(jpaWebServer);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_NAME,
                                          "WebServer Name already exists: " + aWebServer.getCommand().getName());
        }
    }

    @Override
    public WebServer updateWebServer(final Event<UpdateWebServerCommand> aWebServerToUpdate) {

        try {
            final UpdateWebServerCommand updateWebServerCommand = aWebServerToUpdate.getCommand();
            final AuditEvent auditEvent = aWebServerToUpdate.getAuditEvent();
            final Identifier<WebServer> webServerId = updateWebServerCommand.getId();
            final JpaWebServer jpaWebServer = getJpaWebServer(webServerId);

            jpaWebServer.setName(updateWebServerCommand.getNewName());
            jpaWebServer.setPort(updateWebServerCommand.getNewPort());
            jpaWebServer.setHost(updateWebServerCommand.getNewHost());
            jpaWebServer.setUpdateBy(auditEvent.getUser().getUserId());
            jpaWebServer.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

            entityManager.flush();

            return webServerFrom(jpaWebServer);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_NAME,
                                          "WebServer Name already exists: " + aWebServerToUpdate.getCommand().getNewName());
        }
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerFrom(getJpaWebServer(aWebServerId));
    }

    @Override
    public List<WebServer> getWebServers(final PaginationParameter somePagination) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaWebServer> criteria = builder.createQuery(JpaWebServer.class);
        final Root<JpaWebServer> root = criteria.from(JpaWebServer.class);

        criteria.select(root);

        final TypedQuery<JpaWebServer> query = entityManager.createQuery(criteria);

        query.setFirstResult(somePagination.getOffset());
        query.setMaxResults(somePagination.getLimit());

        return webServersFrom(query.getResultList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WebServer> findWebServers(final String aName,
                                  final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaWebServer g WHERE g.name LIKE :WebServerName");
        query.setParameter("WebServerName", "?" + aName + "?");

        query.setFirstResult(somePagination.getOffset());
        query.setMaxResults(somePagination.getLimit());

        return webServersFrom(query.getResultList());
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {

        final JpaWebServer WebServer = getJpaWebServer(aWebServerId);
        entityManager.remove(WebServer);
    }

    protected List<WebServer> webServersFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> WebServers = new ArrayList<>();

        for (final JpaWebServer jpaWebServer : someJpaWebServers) {
            WebServers.add(webServerFrom(jpaWebServer));
        }

        return WebServers;
    }

    protected JpaWebServer getJpaWebServer(final Identifier<WebServer> aWebServer) {

        final JpaWebServer jpaWebServer = entityManager.find(JpaWebServer.class,
                                                     aWebServer.getId());

        if (jpaWebServer == null) {
            throw new NotFoundException(AemFaultType.WEBSERVER_NOT_FOUND,
                                        "WebServer not found: " + aWebServer);
        }

        return jpaWebServer;
    }

    protected WebServer webServerFrom(final JpaWebServer aJpaWebServer) {
        return new JpaWebServerBuilder(aJpaWebServer).build();
    }
}
