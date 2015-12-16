package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import static com.siemens.cto.aem.control.AemControl.Properties.TAR_CREATE_COMMAND;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;

public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger logger = LoggerFactory.getLogger(JvmServiceRestImpl.class);
    public static final String ENTITY_TYPE_JVM = "jvm";
    public static final String CONFIG_FILENAME_INVOKE_BAT = "invoke.bat";

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;
    private final StateService<Jvm, JvmState> jvmStateService;
    private final ResourceService resourceService;
    private final ExecutorService executorService;
    private Map<String, ReentrantReadWriteLock> jvmWriteLocks;
    private final String stpTomcatInstancesPath = ApplicationProperties.get("paths.instances");
    private final String pathsTomcatInstanceTemplatedir = ApplicationProperties.get("paths.tomcat.instance.template");
    private final String stpJvmResourcesDir = ApplicationProperties.get("stp.jvm.resources.dir");

    public JvmServiceRestImpl(final JvmService theJvmService, final JvmControlService theJvmControlService,
            final StateService<Jvm, JvmState> theJvmStateService, final ResourceService theResourceService,
            final ExecutorService theExecutorService, final Map<String, ReentrantReadWriteLock> writeLockMap) {
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
        jvmStateService = theJvmStateService;
        resourceService = theResourceService;
        executorService = theExecutorService;
        jvmWriteLocks = writeLockMap;
    }

    @Override
    public Response getJvms() {
        logger.debug("Get JVMs requested");
        final List<Jvm> jvms = jvmService.getJvms();
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        logger.debug("Get JVM requested: {}", aJvmId);
        return ResponseBuilder.ok(jvmService.getJvm(aJvmId));
    }

    @Override
    public Response createJvm(final JsonCreateJvm aJvmToCreate, final AuthenticatedUser aUser) {
        logger.debug("Create JVM requested: {}", aJvmToCreate);
        final User user = aUser.getUser();

        // create the JVM in the database
        final Jvm jvm;
        if (aJvmToCreate.areGroupsPresent()) {
            jvm = jvmService.createAndAssignJvm(aJvmToCreate.toCreateAndAddCommand(), user);
        } else {
            jvm = jvmService.createJvm(aJvmToCreate.toCreateJvmCommand(), user);
        }

        // upload the default resource templates for the newly created
        // JVM
        uploadAllJvmResourceTemplates(aUser, jvm);

        // set the state to NEW for the newly created JVM
        final CurrentState<Jvm, JvmState> theNewState =
                new CurrentState<>(jvm.getId(), JvmState.JVM_NEW, DateTime.now(), StateType.JVM);
        SetStateRequest<Jvm, JvmState> setStateNewCommand = new JvmSetStateRequest(theNewState);
        jvmStateService.setCurrentState(setStateNewCommand, aUser.getUser());

        return ResponseBuilder.created(jvm);
    }

    void uploadAllJvmResourceTemplates(AuthenticatedUser aUser, final Jvm jvm) {
        for (final ResourceType resourceType : resourceService.getResourceTypes()) {
            if ("jvm".equals(resourceType.getEntityType()) && !"invoke.bat".equals(resourceType.getConfigFileName())) {
                FileInputStream dataInputStream = null;
                try {
                    dataInputStream =
                            new FileInputStream(new File(ApplicationProperties.get("paths.resource-types") + "/"
                                    + resourceType.getTemplateName()));
                    UploadJvmTemplateRequest uploadJvmTemplateCommand =
                            new UploadJvmTemplateRequest(jvm, resourceType.getTemplateName(), dataInputStream) {
                                @Override
                                public String getConfFileName() {
                                    return resourceType.getConfigFileName();
                                }
                            };
                    jvmService.uploadJvmTemplateXml(uploadJvmTemplateCommand, aUser.getUser());
                } catch (FileNotFoundException e) {
                    logger.error("Could not find template {} for new JVM {}", resourceType.getConfigFileName(),
                            jvm.getJvmName(), e);
                    throw new InternalErrorException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Could not find template "
                            + resourceType.getTemplateName());
                }
            }
        }
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate, final AuthenticatedUser aUser) {
        logger.debug("Update JVM requested: {}", aJvmToUpdate);
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmCommand(), aUser.getUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId, final AuthenticatedUser user) {
        logger.info("Delete JVM requested: {}", aJvmId);
        final Jvm jvm = jvmService.getJvm(aJvmId);
        if (!jvmService.isJvmStarted(jvm)) {
            logger.info("Removing JVM from the database and deleting the service");
            jvmService.removeJvm(aJvmId);
            if (!jvmStateService.getCurrentState(aJvmId).getState().equals(JvmState.JVM_NEW)) {
                deleteJvmWindowsService(user, new ControlJvmRequest(aJvmId, JvmControlOperation.DELETE_SERVICE),
                        jvm.getJvmName());
            }
        } else {
            logger.info("JVM was not in stopped state: NO-OP");
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "The target JVM must be stopped before attempting to delete it");
        }
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId, final JsonControlJvm aJvmToControl, final AuthenticatedUser aUser) {
        logger.debug("Control JVM requested: {} {}", aJvmId, aJvmToControl);
        final CommandOutput commandOutput = jvmControlService.controlJvm(new ControlJvmRequest(aJvmId, aJvmToControl.toControlOperation()), aUser.getUser());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput);
        } else {
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, commandOutput.getStandardError());
        }
    }

    @Context
    private MessageContext context;

    /*
     * for unit testing
     */
    void setContext(MessageContext aContext) {
        context = aContext;
    }

    @Override
    public Response uploadConfigTemplate(final String jvmName, final AuthenticatedUser aUser, final String templateName) {
        logger.debug("Upload Archive requested: {} streaming (no size, count yet)", jvmName);

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        Jvm jvm = jvmService.getJvm(jvmName);

        ServletFileUpload sfu = new ServletFileUpload();
        InputStream data = null;
        try {
            FileItemIterator iter = sfu.getItemIterator(context.getHttpServletRequest());
            FileItemStream file1;

            while (iter.hasNext()) {
                file1 = iter.next();
                try {
                    data = file1.openStream();
                    UploadJvmTemplateRequest command = new UploadJvmTemplateRequest(jvm, file1.getName(), data) {
                        @Override
                        public String getConfFileName() {
                            return templateName;
                        }
                    };

                    return ResponseBuilder.created(jvmService.uploadJvmTemplateXml(command, aUser.getUser())); // early
                                                                                                               // out
                                                                                                               // on
                                                                                                               // first
                                                                                                               // attachment
                } finally {
                    assert data != null;
                    data.close();
                }
            }
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_JVM_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response getCurrentJvmStates(final JvmIdsParameterProvider aJvmIdsParameterProvider) {
        logger.debug("Current JVM states requested : {}", aJvmIdsParameterProvider);
        final Set<Identifier<Jvm>> jvmIds = aJvmIdsParameterProvider.valueOf();
        final Set<CurrentState<Jvm, JvmState>> currentJvmStates;

        if (jvmIds.isEmpty()) {
            currentJvmStates = jvmStateService.getCurrentStates();
        } else {
            currentJvmStates = jvmStateService.getCurrentStates(jvmIds);
        }

        return ResponseBuilder.ok(currentJvmStates);
    }

    @Override
    public Response generateConfig(String aJvmName) {
        try {
            String serverXmlStr = jvmService.generateConfigFile(aJvmName, "server.xml");
            return Response.ok(serverXmlStr).build();
        } catch (TemplateNotFoundException e) {
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage(), e);
        }
    }

    @Override
    public Response generateAndDeployConf(final String jvmName, final AuthenticatedUser user) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("jvmName", jvmName);
        errorDetails.put("jvmId", jvmService.getJvm(jvmName).getId().getId().toString());

        // TODO - re-evaluate async choice - since we call .get()
        // there is no benefit.
        try {
            Future<Jvm> futureJvm = executorService.submit(new Callable<Jvm>() {
                @Override
                public Jvm call() throws Exception {
                    final Jvm jvm = jvmService.getJvm(jvmName);
                    return generateAndDeployConf(jvm, user, new RuntimeCommandBuilder());
                }
            });
            return ResponseBuilder.ok(futureJvm.get());
        } catch (RuntimeException | InterruptedException | ExecutionException re) {
            // TODO - just bubble getCause() for ExecutionException
            // and let our Exception Providers handle it.
            logger.error("Failed to generate and deploy configuration files for JVM: {}", jvmName, re);
            if (re.getCause() != null && re.getCause() instanceof InternalErrorException
                    && re.getCause().getCause() != null
                    && re.getCause().getCause() instanceof RemoteCommandFailureException) {
                RemoteCommandFailureException rcfx = (RemoteCommandFailureException) (re.getCause().getCause());
                return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, rcfx.getMessage(), rcfx), errorDetails);
            } else {
                return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, re.getMessage(), re), errorDetails);
            }
        }
    }

    /**
     * Generate and deploy a JVM's configuration files.
     * 
     * @param jvm
     *            - the JVM
     * @param user
     *            - the user
     * @param runtimeCommandBuilder
     */
    Jvm generateAndDeployConf(final Jvm jvm, final AuthenticatedUser user, RuntimeCommandBuilder runtimeCommandBuilder) {

        // only one at a time per JVM
        if (!jvmWriteLocks.containsKey(jvm.getId().toString())) {
            jvmWriteLocks.put(jvm.getId().toString(), new ReentrantReadWriteLock());
        }
        jvmWriteLocks.get(jvm.getId().toString()).writeLock().lock();

        try {
            if (jvmService.isJvmStarted(jvm)) {
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }

            // delete the service
            deleteJvmWindowsService(user, new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE),
                    jvm.getJvmName());

            // create the tar file
            final String jvmConfigTar = generateJvmConfigTar(jvm.getJvmName(), runtimeCommandBuilder);

            // copy the tar file
            secureCopyJvmConfigTar(jvm, jvmConfigTar);

            // call script to backup and tar the current directory and
            // then untar the new tar
            deployJvmConfigTar(jvm, user, jvmConfigTar);

            // re-install the service
            installJvmWindowsService(jvm, user);

            // set the state to stopped
            CurrentState<Jvm, JvmState> stoppedState =
                    new CurrentState<>(jvm.getId(), JvmState.SVC_STOPPED, DateTime.now(), StateType.JVM);
            SetStateRequest<Jvm, JvmState> setStateStoppedCommand = new JvmSetStateRequest(stoppedState);
            jvmStateService.setCurrentState(setStateStoppedCommand, user.getUser());

        } catch (CommandFailureException e) {
            logger.error("Failed to generate the JVM config for {} :: ERROR: {}", jvm.getJvmName(), e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to generate the JVM config",
                    e);
        } finally {
            jvmWriteLocks.get(jvm.getId().toString()).writeLock().unlock();
        }
        return jvm;
    }

    private void installJvmWindowsService(Jvm jvm, AuthenticatedUser user) {
        CommandOutput execData = jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE),
                        user.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            logger.info("Invoke of windows service {} was successful", jvm.getJvmName());
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            logger.error("Invoking windows service {} failed :: ERROR: {}", jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
        }
    }

    private void deployJvmConfigTar(Jvm jvm, AuthenticatedUser user, String jvmConfigTar) {
        CommandOutput execData =jvmControlService.controlJvm(
                        new ControlJvmRequest(jvm.getId(), JvmControlOperation.DEPLOY_CONFIG_TAR), user.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            logger.info("Deployment of config tar was successful: {}", jvmConfigTar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            logger.error(
                    "Deploy command completed with error trying to extract and back up JVM config {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
        }
    }

    private void secureCopyJvmConfigTar(Jvm jvm, String jvmConfigTar) throws CommandFailureException {
        CommandOutput execData;
        execData =
                jvmService.secureCopyFile(new RuntimeCommandBuilder(), jvm.getJvmName() + "_config.tar",
                        stpJvmResourcesDir, jvm.getHostName(), ApplicationProperties.get("paths.instances"));
        if (execData.getReturnCode().wasSuccessful()) {
            logger.info("Copy of config tar successful: {}", jvmConfigTar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            logger.error("Copy command completed with error trying to copy config tar to {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
        }
    }

    private void deleteJvmWindowsService(AuthenticatedUser user, ControlJvmRequest controlJvmRequest, String jvmName) {
        CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmRequest, user.getUser());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            logger.info("Delete of windows service {} was successful", jvmName);
        } else {
            String standardError =
                    commandOutput.getStandardError().isEmpty() ?
                            commandOutput.getStandardOutput() : commandOutput.getStandardError();
            logger.error("Deleting windows service {} failed :: ERROR: {}", jvmName, standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
        }
    }

    @Override
    public Response generateAndDeployFile(final String jvmName, final String fileName, AuthenticatedUser user) {
        Jvm jvm = jvmService.getJvm(jvmName);

        // only one at a time per web server
        if (!jvmWriteLocks.containsKey(jvm.getId().getId().toString())) {
            jvmWriteLocks.put(jvm.getId().getId().toString(), new ReentrantReadWriteLock());
        }
        jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().lock();

        try {
            if (jvmService.isJvmStarted(jvm)) {
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }

            ResourceType deployResource = getResourceTypeTemplate(jvmName, fileName);

            String fileContent = jvmService.generateConfigFile(jvmName, fileName);
            String jvmResourcesNameDir = stpJvmResourcesDir + "/" + jvmName;
            String jvmResourcesDirDest = jvmResourcesNameDir + deployResource.getRelativeDir();
            createConfigFile(jvmResourcesDirDest + "/", fileName, fileContent);

            deployJvmConfigFile(jvmName, fileName, jvm, deployResource, jvmResourcesDirDest);
        } catch (IOException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to write file", e);
        } catch (CommandFailureException ce) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to copy the file", ce);
        } finally {
            jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().unlock();
        }
        return ResponseBuilder.ok(jvm);
    }

    private void deployJvmConfigFile(String jvmName, String fileName, Jvm jvm, ResourceType deployResource,
            String jvmResourcesDirDest) throws CommandFailureException {
        final String destPath =
                stpTomcatInstancesPath + "/" + jvmName + deployResource.getRelativeDir() + "/" + fileName;
        CommandOutput result =
                jvmService.secureCopyFile(new RuntimeCommandBuilder(), fileName, jvmResourcesDirDest,
                        jvm.getHostName(), destPath);
        if (result.getReturnCode().wasSuccessful()) {
            logger.info("Successful generation and deploy of {}", fileName);
        } else {
            String standardError =
                    result.getStandardError().isEmpty() ? result.getStandardOutput() : result.getStandardError();
            logger.error("Copying config file {} failed :: ERROR: {}", fileName, standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
        }
    }

    private ResourceType getResourceTypeTemplate(String jvmName, String fileName) {
        ResourceType deployResource = null;
        for (ResourceType resourceType : resourceService.getResourceTypes()) {
            if (ENTITY_TYPE_JVM.equals(resourceType.getEntityType())
                    && fileName.equals(resourceType.getConfigFileName())) {
                deployResource = resourceType;
                break;
            }
        }
        if (deployResource == null) {
            logger.error("Did not find a template for {} when deploying for JVM {}", fileName, jvmName);
            throw new InternalErrorException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Template not found for " + fileName);
        }
        return deployResource;
    }

    private String generateJvmConfigTar(String jvmName, RuntimeCommandBuilder rtCommandBuilder) {

        String jvmResourcesNameDir = stpJvmResourcesDir + "/" + jvmName;

        try {
            // copy the tomcat instance-template directory
            final File srcDir = new File(pathsTomcatInstanceTemplatedir);
            final File destDir = new File(jvmResourcesNameDir);
            for (String dirPath : srcDir.list()) {
                final File srcChild = new File(srcDir + "/" + dirPath);
                if (srcChild.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(srcChild, destDir);
                } else {
                    FileUtils.copyFileToDirectory(srcChild, destDir);
                }
            }

            // generate the configuration files
            for (ResourceType resourceType : resourceService.getResourceTypes()) {
                if (ENTITY_TYPE_JVM.equals(resourceType.getEntityType())) {
                    String generatedText;
                    if (CONFIG_FILENAME_INVOKE_BAT.equals(resourceType.getConfigFileName())) {
                        // create the invoke.bat separately, since
                        // it's not configurable it's actually NOT in
                        // the database
                        generatedText = jvmService.generateInvokeBat(jvmName);
                    } else {
                        generatedText = jvmService.generateConfigFile(jvmName, resourceType.getConfigFileName());
                    }
                    String jvmResourcesRelativeDir = jvmResourcesNameDir + resourceType.getRelativeDir();
                    createConfigFile(jvmResourcesRelativeDir + "/", resourceType.getConfigFileName(), generatedText);
                }
            }
            createDirectory(jvmResourcesNameDir + "/logs");
        } catch (FileNotFoundException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create file", e);
        } catch (IOException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to write file", e);
        }

        // tar up the test file
        rtCommandBuilder.setOperation(TAR_CREATE_COMMAND);
        String jvmConfigTar = jvmName + "_config.tar";
        rtCommandBuilder.addParameter(jvmConfigTar);
        rtCommandBuilder.addCygwinPathParameter(jvmResourcesNameDir);
        RuntimeCommand tarCommand = rtCommandBuilder.build();
        CommandOutput tarResult = tarCommand.execute();
        if (!tarResult.getReturnCode().wasSuccessful()) {
            String standardError =
                    tarResult.getStandardError().isEmpty() ? tarResult.getStandardOutput() : tarResult
                            .getStandardError();
            logger.error("Tar create command completed with error trying to create config tar for {} :: ERROR: {}",
                    jvmName, standardError);
            throw new InternalErrorException(AemFaultType.INVALID_PATH, standardError);
        }

        return jvmResourcesNameDir;
    }

    private void createConfigFile(String path, String configFileName, String serverXmlStr) throws IOException {
        File serverXml = new File(path + configFileName);
        FileUtils.writeStringToFile(serverXml, serverXmlStr);
    }

    private void createDirectory(String absoluteDirPath) {
        File targetDir = new File(absoluteDirPath);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create directory" + absoluteDirPath);
        }
    }

    @Override
    public Response diagnoseJvm(Identifier<Jvm> aJvmId) {

        String diagnosis = jvmService.performDiagnosis(aJvmId);

        return Response.ok(diagnosis).build();
    }

    @Override
    public Response getResourceNames(final String jvmName) {
        return ResponseBuilder.ok(jvmService.getResourceTemplateNames(jvmName));
    }

    @Override
    public Response getResourceTemplate(final String jvmName, final String resourceTemplateName,
            final boolean tokensReplaced) {
        return ResponseBuilder.ok(jvmService.getResourceTemplate(jvmName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response updateResourceTemplate(final String jvmName, final String resourceTemplateName,
            final String content) {

        try {
            return ResponseBuilder.ok(jvmService.updateResourceTemplate(jvmName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            logger.debug("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewResourceTemplate(final String jvmName, final String groupName, final String template) {
        try {
            return ResponseBuilder.ok(jvmService.previewResourceTemplate(jvmName, groupName, template));
        } catch (RuntimeException rte) {
            logger.debug("Error previewing resource.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

}
