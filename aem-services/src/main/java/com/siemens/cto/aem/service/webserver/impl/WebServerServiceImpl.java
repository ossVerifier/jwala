package com.siemens.cto.aem.service.webserver.impl;

import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.HTTPD_CONF_TEMPLATE;
import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.HTTPD_SSL_CONF_TEMPLATE;
import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.WORKERS_PROPS_TEMPLATE;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.toc.files.TemplateManager;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);

    private final WebServerDao dao;

    private final TemplateManager templateManager;

    public WebServerServiceImpl(final WebServerDao theDao, final TemplateManager theTemplateManager) {
        dao = theDao;
        templateManager = theTemplateManager;
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
    public String generateHttpdConfig(final String aWebServerName, final Boolean withSsl) {
        final WebServer server = dao.findWebServerByName(aWebServerName);
        final List<Application> apps = dao.findApplications(aWebServerName, PaginationParameter.all());
        final List<Jvm> jvms = dao.findJvms(aWebServerName, PaginationParameter.all());
        
        try {
            if (withSsl != null && withSsl) {
                return ApacheWebServerConfigFileGenerator
                            .getHttpdConf(aWebServerName, templateManager.getAbsoluteLocation(HTTPD_SSL_CONF_TEMPLATE), server, jvms, apps);
            }
            return ApacheWebServerConfigFileGenerator
                        .getHttpdConf(aWebServerName, templateManager.getAbsoluteLocation(HTTPD_CONF_TEMPLATE), server, jvms, apps);
        } catch(IOException e) { 
            LOGGER.warn("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateWorkerProperties(final String aWebServerName) {
        final List<Jvm> jvms = dao.findJvms(aWebServerName, PaginationParameter.all());
        final List<Application> apps = dao.findApplications(aWebServerName, PaginationParameter.all());
        try {
            return ApacheWebServerConfigFileGenerator
                    .getWorkersProperties(aWebServerName, templateManager.getAbsoluteLocation(WORKERS_PROPS_TEMPLATE), jvms, apps);
        } catch(IOException e) { 
            LOGGER.warn("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        }
    }

}
