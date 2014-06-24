package com.siemens.cto.aem.service.webserver.impl;

import java.util.List;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.service.webserver.HttpdConfigGenerator;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.service.webserver.WebServerService;

public class WebServerServiceImpl implements WebServerService {

    private WebServerDao dao;

    public WebServerServiceImpl(final WebServerDao theDao) {
        dao = theDao;
    }

    @Override
    @Transactional
    public WebServer createWebServer(final CreateWebServerCommand aCreateWebServerCommand,
                         final User aCreatingUser) {

        aCreateWebServerCommand.validateCommand();

        final Event<CreateWebServerCommand> event = new Event<>(aCreateWebServerCommand,
                                                          AuditEvent.now(aCreatingUser));

        return  dao.createWebServer(event);
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) {

        return dao.getWebServer(aWebServerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> getWebServers(final PaginationParameter aPaginationParam) {

        return dao.getWebServers(aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final String aWebServerNameFragment,
                              final PaginationParameter aPaginationParam) {

        return dao.findWebServers(aWebServerNameFragment,
                            aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final Identifier<Group> aGroupId,
                              final PaginationParameter aPaginationParam) {

        return dao.findWebServersBelongingTo(aGroupId,
                                       aPaginationParam);

    }

    @Override
    @Transactional
    public WebServer updateWebServer(final UpdateWebServerCommand anUpdateWebServerCommand,
                         final User anUpdatingUser) {

        anUpdateWebServerCommand.validateCommand();

        final Event<UpdateWebServerCommand> event = new Event<>(anUpdateWebServerCommand,
                                                          AuditEvent.now(anUpdatingUser));

        return dao.updateWebServer(event);
    }

    @Override
    @Transactional
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {

        dao.removeWebServer(aWebServerId);
    }

    @Override
    @Transactional
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {
        dao.removeWebServersBelongingTo(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateHttpdConfig(final String aWebServerName) {
        List<Application> apps = dao.findApplications(aWebServerName, PaginationParameter.all());
        return HttpdConfigGenerator.getHttpdConf("/httpd-conf.tpl", apps);
    }

}
