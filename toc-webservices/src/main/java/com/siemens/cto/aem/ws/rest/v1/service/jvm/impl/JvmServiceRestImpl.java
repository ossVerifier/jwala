package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JvmServiceRestImpl implements JvmServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceRestImpl.class);
    public static final String ENTITY_TYPE_JVM = "jvm";
    public static final String CONFIG_FILENAME_INVOKE_BAT = "invoke.bat";
    public static final String CONFIG_JAR = "_config.jar";

    private final JvmService jvmService;
    private final JvmControlService jvmControlService;
    private final ResourceService resourceService;
    private final ExecutorService executorService;
    private Map<String, ReentrantReadWriteLock> jvmWriteLocks;
    private final String stpTomcatInstancesPath = ApplicationProperties.get("paths.instances");
    private final String pathsTomcatInstanceTemplatedir = ApplicationProperties.get("paths.tomcat.instance.template");
    private final String stpJvmResourcesDir = ApplicationProperties.get("stp.jvm.resources.dir");
    private static JvmServiceRestImpl instance;

    public JvmServiceRestImpl(final JvmService theJvmService, final JvmControlService theJvmControlService,
                              final ResourceService theResourceService,
                              final ExecutorService theExecutorService, final Map<String, ReentrantReadWriteLock> writeLockMap) {
        jvmService = theJvmService;
        jvmControlService = theJvmControlService;
        resourceService = theResourceService;
        executorService = theExecutorService;
        jvmWriteLocks = writeLockMap;
    }

    @Override
    public Response getJvms() {
        LOGGER.debug("Get JVMs requested");
        final List<Jvm> jvms = jvmService.getJvms();
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getJvm(final Identifier<Jvm> aJvmId) {
        LOGGER.debug("Get JVM requested: {}", aJvmId);
        return ResponseBuilder.ok(jvmService.getJvm(aJvmId));
    }

    @Override
    public Response createJvm(final JsonCreateJvm aJvmToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Create JVM requested: {}", aJvmToCreate);
        final User user = aUser.getUser();

        // create the JVM in the database
        final Jvm jvm;
        if (aJvmToCreate.areGroupsPresent()) {
            jvm = jvmService.createAndAssignJvm(aJvmToCreate.toCreateAndAddRequest(), user);
        } else {
            jvm = jvmService.createJvm(aJvmToCreate.toCreateJvmRequest(), user);
        }

        // upload the default resource templates for the newly created
        // JVM
        uploadAllJvmResourceTemplates(aUser, jvm);

        if (aJvmToCreate.areGroupsPresent()) {
            LOGGER.info("Creating app template for new JVM {}", jvm.getJvmName());
            jvmService.addAppTemplatesForJvm(jvm, aJvmToCreate.toCreateAndAddRequest().getGroups());
        }

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
                    UploadJvmTemplateRequest uploadJvmTemplateRequest =
                            new UploadJvmTemplateRequest(jvm, resourceType.getTemplateName(), dataInputStream) {
                                @Override
                                public String getConfFileName() {
                                    return resourceType.getConfigFileName();
                                }
                            };
                    jvmService.uploadJvmTemplateXml(uploadJvmTemplateRequest, aUser.getUser());
                } catch (FileNotFoundException e) {
                    LOGGER.error("Could not find template {} for new JVM {}", resourceType.getConfigFileName(),
                            jvm.getJvmName(), e);
                    throw new InternalErrorException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Could not find template "
                            + resourceType.getTemplateName());
                }
            }
        }
    }

    @Override
    public Response updateJvm(final JsonUpdateJvm aJvmToUpdate, final AuthenticatedUser aUser) {
        LOGGER.debug("Update JVM requested: {}", aJvmToUpdate);
        return ResponseBuilder.ok(jvmService.updateJvm(aJvmToUpdate.toUpdateJvmRequest(), aUser.getUser()));
    }

    @Override
    public Response removeJvm(final Identifier<Jvm> aJvmId, final AuthenticatedUser user) {
        LOGGER.info("Delete JVM requested: {}", aJvmId);
        final Jvm jvm = jvmService.getJvm(aJvmId);
        if (!jvm.getState().isStartedState()) {
            LOGGER.info("Removing JVM from the database and deleting the service for id {}", aJvmId.getId());
            if (!jvm.getState().equals(JvmState.JVM_NEW)) {
                deleteJvmWindowsService(user, new ControlJvmRequest(aJvmId, JvmControlOperation.DELETE_SERVICE),jvm.getJvmName());
            }
            jvmService.removeJvm(aJvmId);
        } else {
            LOGGER.error("The target JVM {} must be stopped before attempting to delete it", jvm.getJvmName());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "The target JVM must be stopped before attempting to delete it");
        }
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlJvm(final Identifier<Jvm> aJvmId, final JsonControlJvm aJvmToControl, final AuthenticatedUser aUser) {
        LOGGER.debug("Control JVM requested: {} {}", aJvmId, aJvmToControl);
        final CommandOutput commandOutput = jvmControlService.controlJvm(new ControlJvmRequest(aJvmId, aJvmToControl.toControlOperation()), aUser.getUser());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput);
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOutput = commandOutput.getStandardOutput();
            String errMessage = standardError != null && !standardError.isEmpty() ? standardError : standardOutput;
            LOGGER.error("Control JVM unsuccessful: " + errMessage);
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
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
        LOGGER.debug("Upload Archive requested: {} streaming (no size, count yet)", jvmName);

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
                    UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(jvm, file1.getName(), data) {
                        @Override
                        public String getConfFileName() {
                            return templateName;
                        }
                    };

                    return ResponseBuilder.created(jvmService.uploadJvmTemplateXml(uploadJvmTemplateRequest, aUser.getUser())); // early
                    // out
                    // on
                    // first
                    // attachment
                } finally {
                    assert data != null;
                    data.close();
                }
            }
            LOGGER.info("Failed to upload config template {} for JVM {}: No Data", templateName, jvmName);
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_JVM_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            LOGGER.error("Bad Stream: Error receiving data", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response generateConfig(String aJvmName) {
        try {
            String serverXmlStr = jvmService.generateConfigFile(aJvmName, "server.xml");
            return Response.ok(serverXmlStr).build();
        } catch (TemplateNotFoundException e) {
            LOGGER.error("Template Not Found: ", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage(), e);
        }
    }

    @Override
    public Response generateAndDeployConf(final String jvmName, final AuthenticatedUser user) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("jvmName", jvmName);
        final Jvm jvm = jvmService.getJvm(jvmName);
        errorDetails.put("jvmId", jvm.getId().getId().toString());

        try {
            return ResponseBuilder.ok(generateAndDeployConf(jvm, user));
        } catch (RuntimeException re) {
            LOGGER.error("Failed to generate and deploy configuration files for JVM: {}", jvmName, re);
            if (re.getCause() != null && re.getCause() instanceof InternalErrorException
                    && re.getCause().getCause() != null
                    && re.getCause().getCause() instanceof RemoteCommandFailureException) {
                RemoteCommandFailureException rcfx = (RemoteCommandFailureException) (re.getCause().getCause());
                return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, rcfx.getMessage(), rcfx), errorDetails);
            } else {
                return ResponseBuilder.notOkWithDetails(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, re.getMessage() != null ? re.getMessage() : "", re), errorDetails);
            }
        }
    }

    /**
     * Generate and deploy a JVM's configuration files.
     *
     * @param jvm  - the JVM
     * @param user - the user
     */
    Jvm generateAndDeployConf(final Jvm jvm, final AuthenticatedUser user) {

        // only one at a time per JVM
        if (!jvmWriteLocks.containsKey(jvm.getId().toString())) {
            jvmWriteLocks.put(jvm.getId().toString(), new ReentrantReadWriteLock());
        }
        jvmWriteLocks.get(jvm.getId().toString()).writeLock().lock();

        try {
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM {} must be stopped before attempting to update the resource files", jvm.getJvmName());
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }

            // delete the service
            deleteJvmWindowsService(user, new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), jvm.getJvmName());

            // create the tar file
            final String jvmConfigTar = generateJvmConfigTar(jvm.getJvmName());

            // copy the tar file
            secureCopyJvmConfigTar(jvm, jvmConfigTar);

            // call script to backup and tar the current directory and
            // then untar the new tar
            deployJvmConfigTar(jvm, user, jvmConfigTar);

            // deploy any application context xml's in the group
            deployApplicationContextXMLs(jvm);

            // re-install the service
            installJvmWindowsService(jvm, user);

            // set the state to stopped
            jvmService.updateState(jvm.getId(), JvmState.JVM_STOPPED);

        } catch (CommandFailureException e) {
            LOGGER.error("Failed to generate the JVM config for {} :: ERROR: {}", jvm.getJvmName(), e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to generate the JVM config",
                    e);
        } finally {
            jvmWriteLocks.get(jvm.getId().toString()).writeLock().unlock();
        }
        return jvm;
    }

    protected void deployApplicationContextXMLs(Jvm jvm) {
        LOGGER.info("Deploying any application XMLs for applications configured to the group for {}", jvm.getJvmName());
        jvmService.deployApplicationContextXMLs(jvm);
    }

    protected void installJvmWindowsService(Jvm jvm, AuthenticatedUser user) {
        CommandOutput execData = jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE),
                user.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Invoke of windows service {} was successful", jvm.getJvmName());
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error("Invoking windows service {} failed :: ERROR: {}", jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc());
        }
    }

    protected void deployJvmConfigTar(Jvm jvm, AuthenticatedUser user, String jvmConfigTar) {
        CommandOutput execData = jvmControlService.controlJvm(
                new ControlJvmRequest(jvm.getId(), JvmControlOperation.DEPLOY_CONFIG_TAR), user.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Deployment of config tar was successful: {}", jvmConfigTar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error(
                    "Deploy command completed with error trying to extract and back up JVM config {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc());
        }
    }

    protected void secureCopyJvmConfigTar(Jvm jvm, String jvmConfigTar) throws CommandFailureException {
        ControlJvmRequest secureCopyRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY);
        CommandOutput execData;
        String configTarName = jvm.getJvmName() + CONFIG_JAR;
        execData =
                jvmControlService.secureCopyFile(secureCopyRequest, stpJvmResourcesDir + "/" + configTarName, ApplicationProperties.get("paths.instances") + "/" + configTarName);
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Copy of config tar successful: {}", jvmConfigTar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error("Copy command completed with error trying to copy config tar to {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc());
        }
    }

    protected void deleteJvmWindowsService(AuthenticatedUser user, ControlJvmRequest controlJvmRequest, String jvmName) {
        Jvm jvm = jvmService.getJvm(jvmName);
        if (!jvm.getState().equals(JvmState.JVM_NEW)) {
            CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmRequest, user.getUser());
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Delete of windows service {} was successful", jvmName);
            } else {
                String standardError =
                        commandOutput.getStandardError().isEmpty() ?
                                commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}", jvmName, standardError);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    @Override
    public Response generateAndDeployFile(final String jvmName, final String fileName, AuthenticatedUser user) {
        Jvm jvm = jvmService.getJvm(jvmName);

        // only one at a time per jvm
        if (!jvmWriteLocks.containsKey(jvm.getId().getId().toString())) {
            jvmWriteLocks.put(jvm.getId().getId().toString(), new ReentrantReadWriteLock());
        }
        jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().lock();

        try {
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM {} must be stopped before attempting to update the resource files", jvm.getJvmName());
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
            LOGGER.error("Bad Stream: Failed to write file", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to write file", e);
        } catch (CommandFailureException ce) {
            LOGGER.error("Bad Stream: Failed to copy file", ce);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to copy the file", ce);
        } finally {
            jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().unlock();
        }
        return ResponseBuilder.ok(jvm);
    }

    protected void deployJvmConfigFile(String jvmName, String fileName, Jvm jvm, ResourceType deployResource,
                                       String jvmResourcesDirDest) throws CommandFailureException {
        final String destPath =
                stpTomcatInstancesPath + "/" + jvmName + deployResource.getRelativeDir() + "/" + fileName;
        CommandOutput result =
                jvmControlService.secureCopyFileWithBackup(new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY), jvmResourcesDirDest + "/" + fileName, destPath);
        if (result.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successful generation and deploy of {} to {}", fileName, jvmName);
        } else {
            String standardError =
                    result.getStandardError().isEmpty() ? result.getStandardOutput() : result.getStandardError();
            LOGGER.error("Copying config file {} failed :: ERROR: {}", fileName, standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(result.getReturnCode().getReturnCode()).getDesc());
        }
    }

    protected ResourceType getResourceTypeTemplate(String jvmName, String fileName) {
        ResourceType deployResource = null;
        for (ResourceType resourceType : resourceService.getResourceTypes()) {
            if (ENTITY_TYPE_JVM.equals(resourceType.getEntityType())
                    && fileName.equals(resourceType.getConfigFileName())) {
                deployResource = resourceType;
                break;
            }
        }
        if (deployResource == null) {
            LOGGER.error("Did not find a template for {} when deploying for JVM {}", fileName, jvmName);
            throw new InternalErrorException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Template not found for " + fileName);
        }
        return deployResource;
    }

    protected String generateJvmConfigTar(String jvmName) {
        LOGGER.info("Generating JVM configuration tar for {}", jvmName);
        String jvmResourcesNameDir = stpJvmResourcesDir + "/" + jvmName;

        final File generatedDir = new File("./" + jvmName);
        try {
            // copy the tomcat instance-template directory
            final File srcDir = new File(pathsTomcatInstanceTemplatedir);
            final File destDir = generatedDir;
            final String destDirPath = destDir.getAbsolutePath();
            LOGGER.debug("location of template copy: {}", destDirPath);
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
                    String jvmResourcesRelativeDir = destDirPath + resourceType.getRelativeDir();
                    LOGGER.debug("gnerating template in location: {}", jvmResourcesRelativeDir + "/", resourceType.getConfigFileName());
                    createConfigFile(jvmResourcesRelativeDir + "/", resourceType.getConfigFileName(), generatedText);
                }
            }
            createDirectory(destDirPath + "/logs");
        } catch (FileNotFoundException e) {
            LOGGER.error("Bad Stream: Failed to create file", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create file", e);
        } catch (IOException e) {
            LOGGER.error("Bad Stream: Failed to write file", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to write file", e);
        }

        // tar up the file
        String jvmConfigTar = jvmName + CONFIG_JAR;
        JarOutputStream target = null;
        final String configJarPath = stpJvmResourcesDir + "/" + jvmConfigTar;
        try {
            final File jvmResourcesDirFile = new File(stpJvmResourcesDir);
            if (!jvmResourcesDirFile.exists() && !jvmResourcesDirFile.mkdir()) {
                LOGGER.error("Could not create {} for JVM resources generation {}", stpJvmResourcesDir, jvmName);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Could not create " + stpJvmResourcesDir + " for JVM resources generation " + jvmName);
            }
            target = new JarOutputStream(new FileOutputStream(configJarPath));
            add(new File("./" + jvmName + "/"), target);
            target.close();
            LOGGER.debug("created resources at {}, copying to {}", generatedDir.getAbsolutePath(), stpJvmResourcesDir);
            FileUtils.copyDirectoryToDirectory(generatedDir, jvmResourcesDirFile);
            FileUtils.deleteDirectory(generatedDir);
        } catch (IOException e) {
            LOGGER.error("Failed to generate the config jar for {}", jvmName, e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to generate the resources jar for " + jvmName, e);
        }

        LOGGER.info("Generation of configuration tar SUCCEEDED for {}: {}", jvmName, configJarPath);

        return jvmResourcesNameDir;
    }

    private void add(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles())
                    add(nestedFile, target);
                return;
            }

            JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null)
                in.close();
        }
    }

    protected void createConfigFile(String path, String configFileName, String serverXmlStr) throws IOException {
        File serverXml = new File(path + configFileName);
        FileUtils.writeStringToFile(serverXml, serverXmlStr);
    }

    protected void createDirectory(String absoluteDirPath) {
        File targetDir = new File(absoluteDirPath);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            LOGGER.error("Bad Stream: Failed to create directory " + absoluteDirPath);
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
            LOGGER.debug("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewResourceTemplate(final String jvmName, final String groupName, final String template) {
        try {
            return ResponseBuilder.ok(jvmService.previewResourceTemplate(jvmName, groupName, template));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing resource.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

    public static JvmServiceRest get() {
        return instance;
    }
}
