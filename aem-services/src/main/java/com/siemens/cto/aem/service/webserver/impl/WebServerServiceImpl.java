package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.command.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.command.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.command.webserver.UploadHttpdConfTemplateCommand;
import com.siemens.cto.aem.domain.command.webserver.UploadWebServerTemplateCommand;
import com.siemens.cto.aem.domain.command.webserver.UploadWebServerTemplateCommandBuilder;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.*;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);

    private final WebServerDao dao;

    private final FileManager fileManager;

    private final String HTTPD_CONF = "httpd.conf";

    public WebServerServiceImpl(final WebServerDao theDao, final FileManager theFileManager) {
        dao = theDao;
        fileManager = theFileManager;
    }

    @Override
    @Transactional
    public WebServer createWebServer(final CreateWebServerCommand aCreateWebServerCommand,
                                     final User aCreatingUser) {

        aCreateWebServerCommand.validateCommand();

        final Event<CreateWebServerCommand> event = new Event<>(aCreateWebServerCommand,
                AuditEvent.now(aCreatingUser));

        WebServer webServer = dao.createWebServer(event);

        UploadWebServerTemplateCommandBuilder builder = new UploadWebServerTemplateCommandBuilder();
        UploadHttpdConfTemplateCommand uploadHttpdConfTemplateCommand = builder.buildHttpdConfCommand(webServer);
        uploadWebServerConfig(uploadHttpdConfTemplateCommand, aCreatingUser);

        return webServer;
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) {
        return dao.getWebServer(aWebServerId);
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final String aWebServerName) {
        return dao.findWebServerByName(aWebServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> getWebServers() {

        return dao.getWebServers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final String aWebServerNameFragment) {

        return dao.findWebServers(aWebServerNameFragment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final Identifier<Group> aGroupId) {

        return dao.findWebServersBelongingTo(aGroupId);

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
        final List<Application> apps = dao.findApplications(aWebServerName);
        final List<Jvm> jvms = dao.findJvms(aWebServerName);

        try {
            if (withSsl != null && withSsl) {
                String httpdConfText = getResourceTemplate(aWebServerName, "httpd.conf", false);
                return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(aWebServerName, httpdConfText, server, jvms, apps);
            }
            return ApacheWebServerConfigFileGenerator
                    .getHttpdConf(aWebServerName, fileManager.getAbsoluteLocation(HTTPD_CONF_TEMPLATE), server, jvms, apps);
        } catch (IOException e) {
            LOGGER.warn("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        } catch (NonRetrievableResourceTemplateContentException nrtce) {
            // TODO WHAAAAA ???? catchtrycatch - try .... ?
            LOGGER.info("Failed to retrieve resource template from the database", nrtce);
            try {
                return ApacheWebServerConfigFileGenerator
                        .getHttpdConf(aWebServerName, fileManager.getAbsoluteLocation(HTTPD_SSL_CONF_TEMPLATE), server, jvms, apps);
            } catch (IOException e) {
                LOGGER.warn("Template not found", e);
                throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateWorkerProperties(final String aWebServerName) {
        final List<Jvm> jvms = dao.findJvms(aWebServerName);
        final List<Application> apps = dao.findApplications(aWebServerName);
        try {
            return ApacheWebServerConfigFileGenerator
                    .getWorkersProperties(aWebServerName, fileManager.getAbsoluteLocation(WORKERS_PROPS_TEMPLATE), jvms, apps);
        } catch (IOException e) {
            LOGGER.warn("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public List<String> getResourceTemplateNames(String webServerName) {
        return dao.getResourceTemplateNames(webServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public String getResourceTemplate(final String webServerName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = dao.getResourceTemplate(webServerName, resourceTemplateName);
        if (tokensReplaced) {
            if (resourceTemplateName.equalsIgnoreCase(HTTPD_CONF)) {
                return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServerName,
                                                                               template,
                                                                               dao.findWebServerByName(webServerName),
                                                                               dao.findJvms(webServerName),
                                                                               dao.findApplications(webServerName));
            } else {
                throw new UnsupportedOperationException("Data binding for \"" + resourceTemplateName +
                        "\" template is currently not supported");
            }
        }
        return template;
    }

    @Override
    public void populateWebServerConfig(List<UploadWebServerTemplateCommand> uploadWSTemplateCommands, User user, boolean overwriteExisting) {
        dao.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
    }

    @Override
    @Transactional
    public JpaWebServerConfigTemplate uploadWebServerConfig(UploadWebServerTemplateCommand uploadWebServerTemplateCommand, User user) {
        uploadWebServerTemplateCommand.validateCommand();
        final Event<UploadWebServerTemplateCommand> event = new Event<>(uploadWebServerTemplateCommand, AuditEvent.now(user));
        return dao.uploadWebserverConfigTemplate(event);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        dao.updateResourceTemplate(wsName, resourceTemplateName, template);
        return dao.getResourceTemplate(wsName, resourceTemplateName);
    }

    @Override
    @Transactional(readOnly = true)
    public String previewResourceTemplate(final String webServerName, final String groupName, final String template) {
        // TODO: Web server name shouldn't be unique therefore we will have to use the groupName parameter in the future.
        return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServerName,
                                                                       template,
                                                                       dao.findWebServerByName(webServerName),
                                                                       dao.findJvms(webServerName),
                                                                       dao.findApplications(webServerName));
    }

}
