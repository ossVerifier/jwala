package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebServerServiceRestImpl implements WebServerServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class);
    public static final String PATHS_GENERATED_RESOURCE_DIR = "paths.generated.resource.dir";
    private static final String COMMANDS_SCRIPTS_PATH = ApplicationProperties.get("commands.scripts-path");
    private static final String MEDIA_TYPE_TEXT = "text";

    private final WebServerService webServerService;
    private final WebServerControlService webServerControlService;
    private final WebServerCommandService webServerCommandService;
    private final Map<String, ReentrantReadWriteLock> wsWriteLocks;
    private ResourceService resourceService;
    private GroupService groupService;
    private static WebServerServiceRestImpl instance;
    private final BinaryDistributionService binaryDistributionService;
    private static final Long DEFAULT_WAIT_TIMEOUT = 30000L;

    public WebServerServiceRestImpl(final WebServerService theWebServerService,
                                    final WebServerControlService theWebServerControlService,
                                    final WebServerCommandService theWebServerCommandService,
                                    final Map<String, ReentrantReadWriteLock> theWriteLocks,
                                    final ResourceService theResourceService, GroupService groupService,
                                    final BinaryDistributionService binaryDistributionService) {
        webServerService = theWebServerService;
        webServerControlService = theWebServerControlService;
        webServerCommandService = theWebServerCommandService;
        wsWriteLocks = theWriteLocks;
        resourceService = theResourceService;
        this.groupService = groupService;
        this.binaryDistributionService = binaryDistributionService;
    }

    @Override
    public Response getWebServers(final Identifier<Group> aGroupId) {
        LOGGER.debug("Get web servers for group {}", aGroupId);
        final List<WebServer> webServers;
        if (aGroupId == null) {
            webServers = webServerService.getWebServers();
            return ResponseBuilder.ok(webServers);
        }
        webServers = webServerService.findWebServers(aGroupId);
        return ResponseBuilder.ok(webServers);
    }

    @Override
    public Response getWebServer(final Identifier<WebServer> aWsId) {
        LOGGER.debug("Get WS requested: {}", aWsId);
        return ResponseBuilder.ok(webServerService.getWebServer(aWsId));
    }

    @Override
    public Response createWebServer(final JsonCreateWebServer aWebServerToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Create WS requested: {} by user {}", aWebServerToCreate, aUser.getUser().getId());
        try {
            final WebServer webServer = webServerService.createWebServer(aWebServerToCreate.toCreateWebServerRequest(), aUser.getUser());
            // Populate the web server templates from the group templates
            Collection<Group> groups = webServer.getGroups();
            if (null != groups && !groups.isEmpty()) {
                Group group = groups.iterator().next();
                final String groupName = group.getName();
                for (final String templateName : groupService.getGroupWebServersResourceTemplateNames(groupName)) {
                    String templateContent = groupService.getGroupWebServerResourceTemplate(groupName, templateName, false, new ResourceGroup());
                    final String metaDataStr = groupService.getGroupWebServerResourceTemplateMetaData(groupName, templateName);
                    webServerService.uploadWebServerConfig(webServer, templateName, templateContent, metaDataStr, groupName, aUser.getUser());

                }
                if (groups.size() > 1) {
                    LOGGER.warn("Multiple groups were associated with the Web Server, but the Web Server was created using the templates from group " + groupName);
                }
            }
            return ResponseBuilder.created(webServer);

        } catch (EntityExistsException eee) {
            LOGGER.error("Web server \"{}\" already exists", aWebServerToCreate, eee);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_WEBSERVER_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response updateWebServer(final JsonUpdateWebServer aWebServerToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Update WS requested: {} by user {}", aWebServerToCreate, aUser.getUser().getId());
        try {
            return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerRequest(),
                    aUser.getUser()));
        } catch (EntityExistsException eee) {
            LOGGER.error("Web server with name \"{}\" already exists", aWebServerToCreate.toUpdateWebServerRequest().getNewName(), eee);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_WEBSERVER_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response removeWebServer(final Identifier<WebServer> aWsId, final AuthenticatedUser user, final boolean forceDelete) {
        LOGGER.info("Delete WS requested: {} by user {} and forceDelete {}", aWsId, user.getUser().getId(), forceDelete);
        final WebServer webServer = webServerService.getWebServer(aWsId);
        if (!webServerService.isStarted(webServer)) {
            LOGGER.info("Removing web server from the database and deleting the service for id {}", aWsId);
            if (!webServer.getState().equals(WebServerReachableState.WS_NEW) && !forceDelete) {
                try {
                    deleteWebServerWindowsService(user,
                            new ControlWebServerRequest(aWsId, WebServerControlOperation.DELETE_SERVICE), webServer.getName());
                } catch (final RuntimeException e) {
                    return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                            new FaultCodeException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, e.getMessage(), e));
                }
            }
            webServerService.removeWebServer(aWsId);
        } else {
            LOGGER.error("The target web server {} must be stopped before attempting to delete it", webServer.getName());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, "Web server " + webServer.getName() +
                            " must be stopped before it can be deleted!", null));
        }
        return ResponseBuilder.ok("successful");
    }

    @Override
    public Response controlWebServer(final Identifier<WebServer> aWebServerId,
                                     final JsonControlWebServer aWebServerToControl, final AuthenticatedUser aUser,
                                     final Boolean wait, Long waitTimeout) {
        LOGGER.debug("Control Web Server requested: {} {} by user {}", aWebServerId, aWebServerToControl, aUser.getUser().getId());
        final ControlWebServerRequest controlWebServerRequest = new ControlWebServerRequest(aWebServerId, aWebServerToControl.toControlOperation());
        final CommandOutput commandOutput = webServerControlService.controlWebServer(
                controlWebServerRequest,
                aUser.getUser());
        if (Boolean.TRUE.equals(wait)) {
            waitTimeout = waitTimeout == null ? DEFAULT_WAIT_TIMEOUT : waitTimeout * 1000; // wait timeout is in seconds converting it to ms
        }
        if (Boolean.TRUE.equals(wait) && commandOutput.getReturnCode().wasSuccessful() && webServerControlService.waitForState(controlWebServerRequest, waitTimeout)) {
            return ResponseBuilder.ok(commandOutput.getStandardOutput());
        } else if (!Boolean.TRUE.equals(wait) && commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput.getStandardOutput());
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOut = commandOutput.getStandardOutput();
            String errMessage = null != standardError && !standardError.isEmpty() ? standardError : standardOut;
            LOGGER.error("Control Operation Unsuccessful: {}", errMessage);
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
        }
    }

    @Override
    public Response generateConfig(final String webServerName) {
        LOGGER.info("Generate the httpd.conf for {}", webServerName);
        return Response.ok(webServerService.getResourceTemplate(webServerName, "httpd.conf", true,
                resourceService.generateResourceGroup())).build();
    }

    @Override
    public Response generateAndDeployConfig(final String aWebServerName, final String resourceFileName, final AuthenticatedUser user) {
        LOGGER.info("Generate and deploy config {} for web server {} by user {}", resourceFileName, aWebServerName, user.getUser().getId());
        return ResponseBuilder.ok(webServerService.generateAndDeployFile(aWebServerName, resourceFileName, user.getUser()));
    }

    @Override
    public Response generateAndDeployWebServer(final String aWebServerName, final AuthenticatedUser aUser) {
        LOGGER.info("Generate and deploy web server {} by user {}", aWebServerName, aUser.getUser().getId());

        // only one at a time per web server
        if (!wsWriteLocks.containsKey(aWebServerName)) {
            wsWriteLocks.put(aWebServerName, new ReentrantReadWriteLock());
        }
        wsWriteLocks.get(aWebServerName).writeLock().lock();

        try {
            WebServer webServer = webServerService.getWebServer(aWebServerName);
            if (webServerService.isStarted(webServer)) {
                final String errorMessage = "The target Web Server " + aWebServerName + " must be stopped before attempting to update the resource file";
                LOGGER.error(errorMessage);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, errorMessage);
            }
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setWebServerName(webServer.getName())
                    .setResourceName("*")
                    .build();
            resourceService.validateAllResourcesForGeneration(resourceIdentifier);

            binaryDistributionService.prepareUnzip(webServer.getHost());
            binaryDistributionService.distributeWebServer(webServer.getHost());

            // check for httpd.conf template
            checkForHttpdConfTemplate(aWebServerName);

            // create the remote scripts directory
            createScriptsDirectory(webServer);

            // copy the start and stop scripts
            deployStartStopScripts(webServer, aUser.getUser().getId());

            // delete the service
            deleteWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.DELETE_SERVICE), aWebServerName);

            // create the configuration file(s)
            final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
            boolean resourceFileGeneratorExceptionFlag = false;
            for (final String templateName : templateNames) {
                generateAndDeployConfig(aWebServerName, templateName, aUser);
            }

            // re-install the service
            installWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INSTALL_SERVICE), webServer);

            webServerService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);

        } catch (CommandFailureException e) {
            LOGGER.error("Failed for {}", aWebServerName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, e.getMessage(), e));
        } finally {
            wsWriteLocks.get(aWebServerName).writeLock().unlock();
        }

        return ResponseBuilder.ok(webServerService.getWebServer(aWebServerName));
    }

    protected void checkForHttpdConfTemplate(String aWebServerName) {
        boolean foundHttpdConf = false;
        final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
        for (final String templateName : templateNames) {
            if (templateName.equals("httpd.conf")) {
                foundHttpdConf = true;
                break;
            }
        }
        if (!foundHttpdConf) {
            throw new InternalErrorException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND, "No template was found for the httpd.conf. Please upload a template for the httpd.conf and try again.");
        }
    }

    protected void createScriptsDirectory(WebServer webServer) throws CommandFailureException {
        final String scriptsDir = ApplicationProperties.get("remote.commands.user-scripts");
        final CommandOutput result = webServerControlService.createDirectory(webServer, scriptsDir);

        final ExecReturnCode resultReturnCode = result.getReturnCode();
        if (!resultReturnCode.wasSuccessful()) {
            LOGGER.error("Creating scripts directory {} FAILED ", scriptsDir);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(resultReturnCode.getReturnCode()).getDesc());
        }

    }

    protected void deployStartStopScripts(WebServer webServer, String userId) throws CommandFailureException {
        final String webServerName = webServer.getName();
        final String startScriptName = AemControl.Properties.START_SCRIPT_NAME.getValue();
        final String sourceStartServicePath = COMMANDS_SCRIPTS_PATH + "/" + startScriptName;
        final String destHttpdConfParentDir = ApplicationProperties.get("remote.paths.httpd.conf");
        final String destHttpdConfStartScript = destHttpdConfParentDir + "/" + startScriptName;
        if (webServerControlService.createDirectory(webServer, destHttpdConfParentDir).getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully created the directory {}", destHttpdConfParentDir);
        } else {
            LOGGER.error("Failed to create the directory {} during creation of {}", destHttpdConfParentDir, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStartServicePath + " during the creation of " + webServerName);
        }
        if (!webServerControlService.secureCopyFile(webServerName, sourceStartServicePath, destHttpdConfStartScript, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStartServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStartServicePath + " during the creation of " + webServerName);
        }

        final String stopScriptName = AemControl.Properties.STOP_SCRIPT_NAME.getValue();
        final String sourceStopServicePath = COMMANDS_SCRIPTS_PATH + "/" + stopScriptName;
        final String destHttpdConfStopScript = destHttpdConfParentDir + "/" + stopScriptName;
        if (!webServerControlService.secureCopyFile(webServerName, sourceStopServicePath, destHttpdConfStopScript, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStopServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStopServicePath + " during the creation of " + webServerName);
        }

        final String installServiceWsScriptName = AemControl.Properties.INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME.getValue();
        final String sourceInstallServiceWsServicePath = COMMANDS_SCRIPTS_PATH + "/" + installServiceWsScriptName;
        final String jwalaScriptsPath = ApplicationProperties.get("remote.commands.user-scripts");
        if (!webServerControlService.secureCopyFile(webServerName, sourceInstallServiceWsServicePath, jwalaScriptsPath + "/" + installServiceWsScriptName, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceInstallServiceWsServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceInstallServiceWsServicePath + " during the creation of " + webServerName);
        }

        // make sure the scripts are executable
        if (!webServerControlService.changeFileMode(webServer, "a+x", jwalaScriptsPath, "*.sh").getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to update the permissions in {} during the creation of {}", jwalaScriptsPath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to update the permissions in " + sourceInstallServiceWsServicePath + " during the creation of " + webServerName);
        }
        if (!webServerControlService.changeFileMode(webServer, "a+x", destHttpdConfParentDir, "*.sh").getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to update the permissions in {} during the creation of {}", destHttpdConfParentDir, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to update the permissions in " + destHttpdConfParentDir + " during the creation of " + webServerName);
        }
    }

    protected void installWebServerWindowsService(final AuthenticatedUser user, final ControlWebServerRequest installServiceWSBatRequest, final WebServer webServer) throws CommandFailureException {

        // create the install_serviceWS.bat file
        String installserviceWSBatText = webServerService.generateInstallServiceWSBat(webServer);
        final String jwalaGeneratedResourcesDir = ApplicationProperties.get(PATHS_GENERATED_RESOURCE_DIR);
        final String httpdDataDir = ApplicationProperties.get("remote.paths.httpd.conf");
        final String name = webServer.getName();
        final File install_serviceWsBatFile = createTempWebServerResourceFile(name, jwalaGeneratedResourcesDir, "install_serviceWS", "bat", installserviceWSBatText);

        // copy the install_serviceWS.bat file
        final String install_serviceWsBatFileAbsolutePath = install_serviceWsBatFile.getAbsolutePath().replaceAll("\\\\", "/");
        CommandOutput copyResult = webServerControlService.secureCopyFile(name, install_serviceWsBatFileAbsolutePath, httpdDataDir + "/install_serviceWS.bat", user.getUser().getId());
        if (copyResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully copied {} to {}", install_serviceWsBatFileAbsolutePath, webServer.getHost());
        } else {
            LOGGER.error("Failed to copy {} to {} ", install_serviceWsBatFileAbsolutePath, webServer.getHost());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy " + install_serviceWsBatFileAbsolutePath + " to " + webServer.getHost());
        }

        // call the install_serviceWs.bat file
        CommandOutput installserviceResult = webServerControlService.controlWebServer(installServiceWSBatRequest, user.getUser());
        if (installserviceResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully invoked service {}", name);
        } else {
            final String standardError = installserviceResult.getStandardError();
            LOGGER.error("Failed to create windows service for {} :: {}", name, !standardError.isEmpty() ? standardError : installserviceResult.getStandardOutput());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to create windows service for " + name + ". " + installserviceResult.standardErrorOrStandardOut());
        }
    }

    protected void deleteWebServerWindowsService(AuthenticatedUser user, ControlWebServerRequest controlWebServerRequest, String webServerName) {
        WebServer webServer = webServerService.getWebServer(webServerName);
        if (!webServer.getState().equals(WebServerReachableState.WS_NEW)) {
            CommandOutput commandOutput = webServerControlService.controlWebServer(controlWebServerRequest, user.getUser());
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Delete of windows service {} was successful", webServerName);
            } else if (ExecReturnCode.JWALA_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
                LOGGER.info("No such service found for {} during delete. Continuing with request.", webServerName);
            } else {
                String standardError =
                        commandOutput.getStandardError().isEmpty() ?
                                commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}", webServerName, standardError);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
            }
        }
    }

    protected File createTempWebServerResourceFile(String aWebServerName, String httpdDataDir, String fileNamePrefix, String fileNameSuffix, String generatedTemplate) {
        PrintWriter out = null;
        final File httpdConfFile =
                new File(httpdDataDir + System.getProperty("file.separator") + aWebServerName + "_" + fileNamePrefix + "."
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + fileNameSuffix.replace("\\", "/"));
        final String httpdConfAbsolutePath = httpdConfFile.getAbsolutePath().replace("\\", "/");
        try {
            out = new PrintWriter(httpdConfAbsolutePath);
            out.println(generatedTemplate);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create temporary file {}", httpdConfAbsolutePath);
            throw new InternalErrorException(AemFaultType.INVALID_PATH, e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return httpdConfFile;
    }

    @Override
    public Response getHttpdConfig(Identifier<WebServer> aWebServerId) {
        LOGGER.debug("Get httpd.conf for {}", aWebServerId);
        try {
            return Response.ok(webServerCommandService.getHttpdConf(aWebServerId)).build();
        } catch (CommandFailureException cmdFailEx) {
            LOGGER.warn("Request Failure Occurred", cmdFailEx);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, cmdFailEx.getMessage()));
        }
    }

    @Override
    public Response getResourceNames(final String wsName) {
        LOGGER.debug("Get resource names for {}", wsName);
        return ResponseBuilder.ok(webServerService.getResourceTemplateNames(wsName));
    }

    @Override
    public Response getResourceTemplate(final String wsName, final String resourceTemplateName,
                                        final boolean tokensReplaced) {
        LOGGER.debug("Get resource template {} for web server {} : tokens replaced={}", resourceTemplateName, wsName, tokensReplaced);
        return ResponseBuilder.ok(webServerService.getResourceTemplate(wsName, resourceTemplateName, tokensReplaced, resourceService.generateResourceGroup()));
    }

    @Override
    public Response updateResourceTemplate(final String wsName, final String resourceTemplateName, final String content) {
        LOGGER.info("Update the resource template {} for web server {}", resourceTemplateName, wsName);
        LOGGER.debug(content);
        final String someContent = webServerService.updateResourceTemplate(wsName, resourceTemplateName, content);
        if (someContent != null) {
            return ResponseBuilder.ok(someContent);
        } else {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, "Failed to update the template " + resourceTemplateName + " for " + wsName + ". See the log for more details."));
        }

    }

    @Override
    public Response previewResourceTemplate(final String webServerName, final String fileName, final String groupName, String template) {
        LOGGER.debug("Preview resource template {} for web server {} in group {}", template, webServerName, groupName);
        try {
            return ResponseBuilder.ok(webServerService.previewResourceTemplate(fileName, webServerName, groupName, template));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing template.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static WebServerServiceRestImpl get() {
        return instance;
    }
}