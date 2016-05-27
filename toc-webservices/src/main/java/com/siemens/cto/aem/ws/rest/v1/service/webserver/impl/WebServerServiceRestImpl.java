package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ContentType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UploadHttpdConfTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.WebServerServiceRest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebServerServiceRestImpl implements WebServerServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class);
    public static final String STP_HTTPD_DATA_DIR = "paths.httpd.conf";

    private final WebServerService webServerService;
    private final WebServerControlService webServerControlService;
    private final WebServerCommandService webServerCommandService;
    private final Map<String, ReentrantReadWriteLock> wsWriteLocks;
    private ResourceService resourceService;
    private GroupService groupService;
    private static WebServerServiceRestImpl instance;

    public WebServerServiceRestImpl(final WebServerService theWebServerService,
                                    final WebServerControlService theWebServerControlService,
                                    final WebServerCommandService theWebServerCommandService,
                                    final Map<String, ReentrantReadWriteLock> theWriteLocks,
                                    final ResourceService theResourceService, GroupService groupService) {
        webServerService = theWebServerService;
        webServerControlService = theWebServerControlService;
        webServerCommandService = theWebServerCommandService;
        wsWriteLocks = theWriteLocks;
        resourceService = theResourceService;
        this.groupService = groupService;
    }

    @Override
    public Response getWebServers(final Identifier<Group> aGroupId) {
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
        LOGGER.info("Create WS requested: {}", aWebServerToCreate);
        final WebServer webServer = webServerService.createWebServer(aWebServerToCreate.toCreateWebServerRequest(), aUser.getUser());
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
                    UploadWebServerTemplateRequest uploadWSRequest = new UploadWebServerTemplateRequest(webServer, metaData.getTemplateName(), metaDataStr, new ByteArrayInputStream(templateContent.getBytes())) {
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
        LOGGER.debug("Update WS requested: {}", aWebServerToCreate);
        return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerRequest(),
                aUser.getUser()));
    }

    @Override
    public Response removeWebServer(final Identifier<WebServer> aWsId, final AuthenticatedUser user) {
        LOGGER.debug("Delete WS requested: {}", aWsId);
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
        LOGGER.debug("Control Web Server requested: {} {}", aWebServerId, aWebServerToControl);
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
        return Response.ok(webServerService.getResourceTemplate(webServerName, "httpd.conf", true,
                           resourceService.generateResourceGroup())).build();
    }

    @Override
    public Response generateAndDeployConfig(final String aWebServerName, final String resourceFileName, final boolean doBackup) {

        // only one at a time per web server
        if (!wsWriteLocks.containsKey(aWebServerName)) {
            wsWriteLocks.put(aWebServerName, new ReentrantReadWriteLock());
        }

        wsWriteLocks.get(aWebServerName).writeLock().lock();

        try {
            // create the file
            final String httpdDataDir = ApplicationProperties.get(STP_HTTPD_DATA_DIR);
            final String generatedHttpdConf = webServerService.getResourceTemplate(aWebServerName, resourceFileName, true,
                    resourceService.generateResourceGroup());
            int resourceNameDotIndex = resourceFileName.lastIndexOf(".");
            final File httpdConfFile = createTempWebServerResourceFile(aWebServerName, httpdDataDir, resourceFileName.substring(0, resourceNameDotIndex), resourceFileName.substring(resourceNameDotIndex, resourceFileName.length() - 1), generatedHttpdConf);

            // copy the file
            final CommandOutput execData;
            final String httpdUnixPath = httpdConfFile.getAbsolutePath().replace("\\", "/");

            if (webServerService.isStarted(webServerService.getWebServer(aWebServerName))) {
                LOGGER.error("The target Web Server {} must be stopped before attempting to update the resource file", aWebServerName);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "The target Web Server must be stopped before attempting to update the resource file");
            }

            String metaDataStr = webServerService.getResourceTemplateMetaData(aWebServerName, resourceFileName);
            ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);

            String metaDataPath;
            if (metaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
                metaDataPath =  webServerService.getResourceTemplate(aWebServerName, resourceFileName, false, resourceService.generateResourceGroup());
            } else {
                metaDataPath = ResourceFileGenerator.generateResourceConfig(metaData.getDeployPath(), resourceService.generateResourceGroup(), webServerService.getWebServer(aWebServerName)) + "/" + resourceFileName;
            }
            execData = webServerControlService.secureCopyFileWithBackup(aWebServerName, httpdUnixPath, metaDataPath, doBackup);
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of {} successful: {}", resourceFileName, httpdUnixPath);
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
    public Response generateAndDeployWebServer(final String aWebServerName, boolean doBackup, final AuthenticatedUser aUser) {
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

            // create the remote scripts directory
            createScriptsDirectory(webServer);

            // copy the start and stop scripts
            deployStartStopScripts(webServer);

            // delete the service
            deleteWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.DELETE_SERVICE), aWebServerName);


            boolean doNotBackupFilesForNewWebServer = !WebServerReachableState.WS_NEW.equals(webServer.getState()) && doBackup;

            // create the configuration file(s)
            final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
            for (final String templateName: templateNames) {
                generateAndDeployConfig(aWebServerName, templateName, doNotBackupFilesForNewWebServer);
            }

            // re-install the service
            installWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INVOKE_SERVICE), webServer, doNotBackupFilesForNewWebServer);

            webServerService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);

        } catch (CommandFailureException e) {
            LOGGER.error("Failed to secure copy the invokeWS.bat file for {}", aWebServerName, e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy the invokeWS.bat file for " + aWebServerName, e);
        } catch (InternalErrorException e) {
            if (e.getMessageResponseStatus().equals(AemFaultType.TEMPLATE_NOT_FOUND)) {
                throw new InternalErrorException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND, "Failed to generate " + aWebServerName + " because no httpd.conf template has been defined yet.Upload a httpd.conf template for this web server and try again.");
            } else {
                throw e;
            }
        } finally {
            wsWriteLocks.get(aWebServerName).writeLock().unlock();
        }
        return ResponseBuilder.ok(webServerService.getWebServer(aWebServerName));
    }

    protected void createScriptsDirectory(WebServer webServer) throws CommandFailureException {
        final String scriptsDir = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();

        final CommandOutput result = webServerControlService.createDirectory(webServer, scriptsDir);

        final ExecReturnCode resultReturnCode = result.getReturnCode();
        if (!resultReturnCode.wasSuccessful()) {
            LOGGER.error("Creating scripts directory {} FAILED ", scriptsDir);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(resultReturnCode.getReturnCode()).getDesc());
        }

    }

    protected void deployStartStopScripts(WebServer webServer) throws CommandFailureException {
        final String webServerName = webServer.getName();
        final boolean doNotBackup = false;
        final String destHttpdConfPath = ApplicationProperties.get("paths.webserver.conf", ApplicationProperties.get("paths.httpd.conf")) + "/";

        final String sourceStartServicePath = AemControl.Properties.SCRIPTS_PATH + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue();
        if (!webServerControlService.secureCopyFileWithBackup(webServerName, sourceStartServicePath, destHttpdConfPath, doNotBackup).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStartServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStartServicePath + " during the creation of " + webServerName);
        }

        final String sourceStopServicePath = AemControl.Properties.SCRIPTS_PATH + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue();
        if (!webServerControlService.secureCopyFileWithBackup(webServerName, sourceStopServicePath, destHttpdConfPath, doNotBackup).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceStopServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceStopServicePath + " during the creation of " + webServerName);
        }

        final String sourceInvokeWsServicePath = AemControl.Properties.SCRIPTS_PATH + "/" + AemControl.Properties.INVOKE_WS_SERVICE_SCRIPT_NAME.getValue();
        final String tocScriptsPath = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();
        if (!webServerControlService.secureCopyFileWithBackup(webServerName, sourceInvokeWsServicePath, tocScriptsPath, doNotBackup).getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to secure copy file {} during creation of {}", sourceInvokeWsServicePath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to secure copy file " + sourceInvokeWsServicePath + " during the creation of " + webServerName);
        }

        // make sure the scripts are executable
        if (!webServerControlService.changeFileMode(webServer, "a+x", tocScriptsPath, "*.sh").getReturnCode().wasSuccessful()) {
            LOGGER.error("Failed to update the permissions in {} during the creation of {}", tocScriptsPath, webServerName);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to update the permissions in " + sourceInvokeWsServicePath + " during the creation of " + webServerName);
        }
    }

    protected void installWebServerWindowsService(final AuthenticatedUser user, final ControlWebServerRequest invokeWSBatRequest, final WebServer webServer, final boolean doBackup) throws CommandFailureException {

        // create the file
        String invokeWSBatText = webServerService.generateInvokeWSBat(webServer); // TODO: Ask this question "Can we just generate and deploy all the resources attached to the web server rather than having a specific methods for a particular file ?"
        final String httpdDataDir = ApplicationProperties.get(STP_HTTPD_DATA_DIR);
        final String name = webServer.getName();
        final File invokeWsBatFile = createTempWebServerResourceFile(name, httpdDataDir, "invokeWS", "bat", invokeWSBatText);

        // copy the invokeWs.bat file
        final String invokeWsBatFileAbsolutePath = invokeWsBatFile.getAbsolutePath();
        CommandOutput copyResult = webServerControlService.secureCopyFileWithBackup(name, invokeWsBatFileAbsolutePath, httpdDataDir + "/invokeWS.bat", doBackup);
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
            } else if (ExecReturnCode.STP_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
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
                new File((httpdDataDir + System.getProperty("file.separator") + aWebServerName + "_" + fileNamePrefix + "."
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "." + fileNameSuffix).replace("\\", "/"));
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
    public Response uploadConfigTemplate(String webServerName, AuthenticatedUser aUser, String templateName) {
        LOGGER.debug("Upload Archive requested: {} streaming (no size, count yet)", webServerName);

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        WebServer webServer = webServerService.getWebServer(webServerName);
        if (null == webServer) {
            LOGGER.error("Web Server Not Found: Could not find web server with name " + webServerName);
            throw new InternalErrorException(AemFaultType.WEBSERVER_NOT_FOUND, "Could not find web server with name " + webServerName);
        }

        ServletFileUpload sfu = new ServletFileUpload();
        InputStream data = null;
        try {
            FileItemIterator iter = sfu.getItemIterator(context.getHttpServletRequest());
            FileItemStream file1;

            while (iter.hasNext()) {
                file1 = iter.next();
                try {
                    data = file1.openStream();
                    UploadWebServerTemplateRequest request =
                            new UploadHttpdConfTemplateRequest(webServer, file1.getName(), data);

                    return ResponseBuilder.created(webServerService.uploadWebServerConfig(request, aUser.getUser())); // early
                    // out
                    // on
                    // first
                    // attachment
                } finally {
                    assert data != null;
                    data.close();
                }
            }
            LOGGER.info("Failed to upload config template {} for web server {}: No Data", templateName, webServerName);
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            LOGGER.error("Bad Stream: Error receiving data", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response getResourceTemplate(final String wsName, final String resourceTemplateName,
                                        final boolean tokensReplaced) {
        return ResponseBuilder.ok(webServerService.getResourceTemplate(wsName, resourceTemplateName, tokensReplaced, resourceService.generateResourceGroup()));
    }

    @Override
    public Response updateResourceTemplate(final String wsName, final String resourceTemplateName, final String content) {

        try {
            return ResponseBuilder.ok(webServerService.updateResourceTemplate(wsName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update resource template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewResourceTemplate(final String webServerName, final String groupName, String template) {
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
