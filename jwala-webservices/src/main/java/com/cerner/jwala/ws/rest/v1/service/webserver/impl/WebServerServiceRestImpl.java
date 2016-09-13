package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ContentType;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
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
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.template.ResourceFileGenerator;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final WebServerService webServerService;
    private final WebServerControlService webServerControlService;
    private final WebServerCommandService webServerCommandService;
    private final Map<String, ReentrantReadWriteLock> wsWriteLocks;
    private ResourceService resourceService;
    private GroupService groupService;
    private static WebServerServiceRestImpl instance;
    private final BinaryDistributionService binaryDistributionService;

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
        final WebServer webServer = webServerService.createWebServer(aWebServerToCreate.toCreateWebServerRequest(), aUser.getUser());

        // Populate the web server templates from the group templates
        Collection<Group> groups = webServer.getGroups();
        if (null != groups && groups.size() > 0) {
            Group group = groups.iterator().next();
            final String groupName = group.getName();
            for (final String templateName : groupService.getGroupWebServersResourceTemplateNames(groupName)) {
                String templateContent = groupService.getGroupWebServerResourceTemplate(groupName, templateName, false, new ResourceGroup());
                String metaDataStr = groupService.getGroupWebServerResourceTemplateMetaData(groupName, templateName);
                ResourceTemplateMetaData metaData;
                try {
                    metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                    UploadWebServerTemplateRequest uploadWSRequest = new UploadWebServerTemplateRequest(webServer, metaData.getTemplateName(), metaDataStr, templateContent) {
                        @Override
                        public String getConfFileName() {
                            return templateName;
                        }
                    };
                    webServerService.uploadWebServerConfig(uploadWSRequest, aUser.getUser());
                } catch (IOException e) {
                    LOGGER.error("Failed to map meta data when creating template {} for web server {}", templateName, webServer.getName(), e);
                    return ResponseBuilder.notOk(Response.Status.EXPECTATION_FAILED, new FaultCodeException(AemFaultType.BAD_STREAM, "Created web server " + webServer.getName() + " but failed creating templates from parent group " + groupName, e));
                }
            }
            if (groups.size() > 1) {
                return ResponseBuilder.notOk(Response.Status.EXPECTATION_FAILED, new FaultCodeException(AemFaultType.GROUP_NOT_SPECIFIED, "Multiple groups were associated with the Web Server, but the Web Server was created using the templates from group " + groupName));
            }
        }
        return ResponseBuilder.created(webServer);
    }

    @Override
    public Response updateWebServer(final JsonUpdateWebServer aWebServerToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Update WS requested: {} by user {}", aWebServerToCreate, aUser.getUser().getId());
        return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerRequest(),
                aUser.getUser()));
    }

    @Override
    public Response removeWebServer(final Identifier<WebServer> aWsId, final AuthenticatedUser user) {
        LOGGER.info("Delete WS requested: {} by user {}", aWsId, user.getUser().getId());
        final WebServer webServer = webServerService.getWebServer(aWsId);
        if (!webServerService.isStarted(webServer)) {
            LOGGER.info("Removing web server from the database and deleting the service for id {}", aWsId);
            if (!webServer.getState().equals(WebServerReachableState.WS_NEW)) {
                deleteWebServerWindowsService(user, new ControlWebServerRequest(aWsId, WebServerControlOperation.DELETE_SERVICE), webServer.getName());
            }
            webServerService.removeWebServer(aWsId);
        } else {
            LOGGER.error("The target web server {} must be stopped before attempting to delete it", webServer.getName());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "The target web server must be stopped before attempting to delete it");
        }

        return ResponseBuilder.ok();
    }

    @Override
    public Response controlWebServer(final Identifier<WebServer> aWebServerId,
                                     final JsonControlWebServer aWebServerToControl, final AuthenticatedUser aUser) {
        LOGGER.debug("Control Web Server requested: {} {} by user {}", aWebServerId, aWebServerToControl, aUser.getUser().getId());
        final CommandOutput commandOutput = webServerControlService.controlWebServer(
                new ControlWebServerRequest(aWebServerId, aWebServerToControl.toControlOperation()),
                aUser.getUser());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput.getStandardOutput());
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOut = commandOutput.getStandardOutput();
            String errMessage = null != standardError && !standardError.isEmpty() ? standardError : standardOut;
            LOGGER.error("Control Operation Unsuccessful: " + errMessage);
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

        // only one at a time per web server
        if (!wsWriteLocks.containsKey(aWebServerName)) {
            wsWriteLocks.put(aWebServerName, new ReentrantReadWriteLock());
        }

        wsWriteLocks.get(aWebServerName).writeLock().lock();

        try {
            // check the web server state
            if (webServerService.isStarted(webServerService.getWebServer(aWebServerName))) {
                LOGGER.error("The target Web Server {} must be stopped before attempting to update the resource file", aWebServerName);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "The target Web Server must be stopped before attempting to update the resource file");
            }

            // get the meta data
            String metaDataStr = webServerService.getResourceTemplateMetaData(aWebServerName, resourceFileName);
            ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);

            String configFilePath;
            if (metaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                configFilePath = webServerService.getResourceTemplate(aWebServerName, resourceFileName, false, resourceService.generateResourceGroup());
            } else {
                // create the file
                final String jwalaGeneratedResourcesDir = ApplicationProperties.get(PATHS_GENERATED_RESOURCE_DIR);
                final String generatedHttpdConf = webServerService.getResourceTemplate(aWebServerName, resourceFileName, true,
                        resourceService.generateResourceGroup());
                int resourceNameDotIndex = resourceFileName.lastIndexOf(".");
                final File configFile = createTempWebServerResourceFile(aWebServerName, jwalaGeneratedResourcesDir, resourceFileName.substring(0, resourceNameDotIndex), resourceFileName.substring(resourceNameDotIndex + 1, resourceFileName.length()), generatedHttpdConf);

                // copy the file
                configFilePath = configFile.getAbsolutePath().replace("\\", "/");
            }
            String destinationPath = ResourceFileGenerator.generateResourceConfig(metaData.getDeployPath(), resourceService.generateResourceGroup(), webServerService.getWebServer(aWebServerName)) + "/" + resourceFileName;
            final CommandOutput execData;
            execData = webServerControlService.secureCopyFile(aWebServerName, configFilePath, destinationPath, user.getUser().getId());
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of {} successful: {}", resourceFileName, configFilePath);
            } else {
                String standardError =
                        execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData
                                .getStandardError();
                LOGGER.error("Copy command completed with error trying to copy httpd.conf to {} :: ERROR: {}",
                        aWebServerName, standardError);
                Map<String, String> errorDetails = new HashMap<>();
                errorDetails.put("webServerName", aWebServerName);
                return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc()), errorDetails);
            }
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to copy the httpd.conf to {} :: ERROR: {}", aWebServerName, e);
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("webServerName", aWebServerName);
            return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy httpd.conf"), errorDetails);
        } catch (JsonMappingException | JsonParseException e) {
            LOGGER.error("Failed to map meta data for {}", aWebServerName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(AemFaultType.BAD_STREAM, "Failed to map meta data for " + aWebServerName, e));
        } catch (IOException e) {
            LOGGER.error("Failed to map meta data because of IOException for {}", aWebServerName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(AemFaultType.BAD_STREAM, "Failed to map meta data because of IOException for " + aWebServerName, e));
        } finally {
            wsWriteLocks.get(aWebServerName).writeLock().unlock();
        }
        return ResponseBuilder.ok(webServerService.getResourceTemplate(aWebServerName, resourceFileName, false, new ResourceGroup()));
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
                LOGGER.error("The target Web Server {} must be stopped before attempting to update the resource file", aWebServerName);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "The target Web Server must be stopped before attempting to update the resource file");
            }

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
            for (final String templateName: templateNames) {
                generateAndDeployConfig(aWebServerName, templateName, aUser);
            }

            // re-install the service
            installWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INVOKE_SERVICE), webServer);

            webServerService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);

        } catch (CommandFailureException e) {
            LOGGER.error("Failed to secure copy the invokeWS.bat file for {}", aWebServerName, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy the invokeWS.bat file for " + aWebServerName, e);
        } finally {
            wsWriteLocks.get(aWebServerName).writeLock().unlock();
        }
        return ResponseBuilder.ok(webServerService.getWebServer(aWebServerName));
    }

    protected void checkForHttpdConfTemplate(String aWebServerName) {
        boolean foundHttpdConf = false;
        final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
        for (final String templateName : templateNames) {
            if (templateName.equals("httpd.conf")){
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
        final String destHttpdConfPath = ApplicationProperties.get("remote.paths.httpd.conf") + "/";

        final String startScriptName = AemControl.Properties.START_SCRIPT_NAME.getValue();
        final String sourceStartServicePath = COMMANDS_SCRIPTS_PATH + "/" + startScriptName;
        if (!webServerControlService.secureCopyFile(webServerName, sourceStartServicePath, destHttpdConfPath, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStartServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStartServicePath + " during the creation of " + webServerName);
        }

        final String stopScriptName = AemControl.Properties.STOP_SCRIPT_NAME.getValue();
        final String sourceStopServicePath = COMMANDS_SCRIPTS_PATH + "/" + stopScriptName;
        if (!webServerControlService.secureCopyFile(webServerName, sourceStopServicePath, destHttpdConfPath, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStopServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStopServicePath + " during the creation of " + webServerName);
        }

        final String invokeWsScriptName = AemControl.Properties.INVOKE_WS_SERVICE_SCRIPT_NAME.getValue();
        final String sourceInvokeWsServicePath = COMMANDS_SCRIPTS_PATH + "/" + invokeWsScriptName;
        final String jwalaScriptsPath = ApplicationProperties.get("remote.commands.user-scripts");
        if (!webServerControlService.secureCopyFile(webServerName, sourceInvokeWsServicePath, jwalaScriptsPath, userId).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceInvokeWsServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceInvokeWsServicePath + " during the creation of " + webServerName);
        }

        // make sure the scripts are executable
        if (!webServerControlService.changeFileMode(webServer, "a+x", jwalaScriptsPath, "*.sh").getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to update the permissions in {} during the creation of {}", jwalaScriptsPath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to update the permissions in " + sourceInvokeWsServicePath + " during the creation of " + webServerName);
        }
        if (!webServerControlService.changeFileMode(webServer, "a+x", destHttpdConfPath, "*.sh").getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to update the permissions in {} during the creation of {}", destHttpdConfPath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to update the permissions in " + destHttpdConfPath+ " during the creation of " + webServerName);
        }
    }

    protected void installWebServerWindowsService(final AuthenticatedUser user, final ControlWebServerRequest invokeWSBatRequest, final WebServer webServer) throws CommandFailureException {

        // create the invokeWs.bat file
        String invokeWSBatText = webServerService.generateInvokeWSBat(webServer);
        final String jwalaGeneratedResourcesDir = ApplicationProperties.get(PATHS_GENERATED_RESOURCE_DIR);
        final String httpdDataDir = ApplicationProperties.get("remote.paths.httpd.conf");
        final String name = webServer.getName();
        final File invokeWsBatFile = createTempWebServerResourceFile(name, jwalaGeneratedResourcesDir, "invokeWS", "bat", invokeWSBatText);

        // copy the invokeWs.bat file
        final String invokeWsBatFileAbsolutePath = invokeWsBatFile.getAbsolutePath().replaceAll("\\\\", "/");
        CommandOutput copyResult = webServerControlService.secureCopyFile(name, invokeWsBatFileAbsolutePath, httpdDataDir + "/invokeWS.bat", user.getUser().getId());
        if (copyResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully copied {} to {}", invokeWsBatFileAbsolutePath, webServer.getHost());
        } else {
            LOGGER.error("Failed to copy {} to {} ", invokeWsBatFileAbsolutePath, webServer.getHost());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy " + invokeWsBatFileAbsolutePath + " to " + webServer.getHost());
        }

        // call the invokeWs.bat file
        CommandOutput invokeResult = webServerControlService.controlWebServer(invokeWSBatRequest, user.getUser());
        if (invokeResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully invoked service {}", name);
        } else {
            final String standardError = invokeResult.getStandardError();
            LOGGER.error("Failed to create windows service for {} :: {}", name, !standardError.isEmpty() ? standardError : invokeResult.getStandardOutput());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to created windows service for " + name);
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
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
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

    @Context
    private MessageContext context;

    /*
     * access point for unit testing
     */
    void setMessageContext(MessageContext aContextForTesting) {
        context = aContextForTesting;
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
    public Response previewResourceTemplate(final String webServerName, final String groupName, String template) {
        LOGGER.debug("Preview resource template {} for web server {} in group {}", template, webServerName, groupName);
        try {
            return ResponseBuilder.ok(webServerService.previewResourceTemplate(webServerName, groupName, template));
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
