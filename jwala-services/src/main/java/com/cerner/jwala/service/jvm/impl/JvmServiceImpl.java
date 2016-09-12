package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ContentType;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.files.FileManager;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.cerner.jwala.template.ResourceFileGenerator;
import groovy.text.SimpleTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);

    private String topicServerStates;
    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final ApplicationService applicationService;
    private final FileManager fileManager;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupStateNotificationService groupStateNotificationService;
    private final ResourceService resourceService;
    private final ClientFactoryHelper clientFactoryHelper;
    private final JvmControlService jvmControlService;
    private final Map<String, ReentrantReadWriteLock> jvmWriteLocks;
    private final BinaryDistributionService binaryDistributionService;

    private static final String DIAGNOSIS_INITIATED = "Diagnosis Initiated on JVM ${jvm.jvmName}, host ${jvm.hostName}";
    private static final String TEMPLATE_DIR = ApplicationProperties.get("paths.tomcat.instance.template");
    public static final String CONFIG_FILENAME_INVOKE_BAT = "invoke.bat";
    public static final String CONFIG_JAR = "_config.jar";

    public JvmServiceImpl(final JvmPersistenceService jvmPersistenceService,
                          final GroupService groupService,
                          final ApplicationService applicationService,
                          final FileManager fileManager,
                          final SimpMessagingTemplate messagingTemplate,
                          final GroupStateNotificationService groupStateNotificationService,
                          final ResourceService resourceService,
                          final ClientFactoryHelper clientFactoryHelper,
                          final String topicServerStates,
                          final JvmControlService jvmControlService,
                          final Map<String, ReentrantReadWriteLock> writeLockMap,
                          final BinaryDistributionService binaryDistributionService) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.fileManager = fileManager;
        this.messagingTemplate = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
        this.resourceService = resourceService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.jvmControlService = jvmControlService;
        this.topicServerStates = topicServerStates;
        jvmWriteLocks = writeLockMap;
        this.binaryDistributionService = binaryDistributionService;
    }

    protected Jvm createJvm(final CreateJvmRequest aCreateJvmRequest) {
        aCreateJvmRequest.validate();
        return jvmPersistenceService.createJvm(aCreateJvmRequest);
    }

    protected Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsRequest aCreateAndAssignRequest,
                                     final User aCreatingUser) {
        // The commands are validated in createJvm() and groupService.addJvmToGroup()
        final Jvm newJvm = createJvm(aCreateAndAssignRequest.getCreateCommand());

        if (!aCreateAndAssignRequest.getGroups().isEmpty()) {
            final Set<AddJvmToGroupRequest> addJvmToGroupRequests = aCreateAndAssignRequest.toAddRequestsFor(newJvm.getId());
            addJvmToGroups(addJvmToGroupRequests, aCreatingUser);
        }

        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional
    public Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user) {
        // create the JVM in the database
        final Jvm jvm = createAndAssignJvm(createJvmAndAddToGroupsRequest, user);

        // inherit the templates from the group
        if (null != jvm.getGroups() && jvm.getGroups().size() > 0) {
            final Group parentGroup = jvm.getGroups().iterator().next();
            createDefaultTemplates(jvm.getJvmName(), parentGroup);
            if (jvm.getGroups().size() > 1) {
                LOGGER.warn("Multiple groups were associated with the JVM, but the JVM was created using the templates from group " + parentGroup.getName());
            }
        }

        return jvm;
    }

    @Override
    @Transactional
    public void createDefaultTemplates(final String jvmName, Group parentGroup) {
        final String groupName = parentGroup.getName();
        // get the group JVM templates
        List<String> templateNames = groupService.getGroupJvmsResourceTemplateNames(groupName);
        for (final String templateName : templateNames) {
            String templateContent = groupService.getGroupJvmResourceTemplate(groupName, templateName, resourceService.generateResourceGroup(), false);
            String metaDataStr = groupService.getGroupJvmResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                        .setResourceName(metaData.getTemplateName())
                        .setJvmName(jvmName)
                        .setGroupName(groupName)
                        .build();
                resourceService.createResource(resourceIdentifier, metaData, IOUtils.toInputStream(templateContent));

            } catch (IOException e) {
                LOGGER.error("Failed to map meta data for JVM {} in group {}", jvmName, groupName, e);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to map meta data for JVM " + jvmName + " in group " + groupName, e);
            }
        }

        // get the group App templates
        templateNames = groupService.getGroupAppsResourceTemplateNames(groupName);
        for (String templateName : templateNames) {
            String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                if (metaData.getEntity().getDeployToJvms()) {
                    final String template = resourceService.getAppTemplate(groupName, metaData.getEntity().getTarget(), templateName);
                    final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                            .setResourceName(metaData.getTemplateName()).setJvmName(jvmName)
                            .setWebAppName(metaData.getEntity().getTarget()).build();
                    resourceService.createResource(resourceIdentifier, metaData, new ByteArrayInputStream(template.getBytes()));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to map meta data while creating JVM for template {} in group {}", templateName, groupName, e);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to map data for template " + templateName + " in group " + groupName, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {
        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final String jvmName) {
        return jvmPersistenceService.findJvmByExactName(jvmName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms() {

        return jvmPersistenceService.getJvms();
    }

    @Override
    @Transactional
    public Jvm updateJvm(final UpdateJvmRequest updateJvmRequest,
                         final User anUpdatingUser) {

        updateJvmRequest.validate();

        jvmPersistenceService.removeJvmFromGroups(updateJvmRequest.getId());

        addJvmToGroups(updateJvmRequest.getAssignmentCommands(), anUpdatingUser);

        return jvmPersistenceService.updateJvm(updateJvmRequest);
    }

    @Override
    @Transactional
    public void removeJvm(final Identifier<Jvm> aJvmId, User user) {
        final Jvm jvm = getJvm(aJvmId);
        if (!jvm.getState().isStartedState()) {
            LOGGER.info("Removing JVM from the database and deleting the service for id {}", aJvmId.getId());
            if (!jvm.getState().equals(JvmState.JVM_NEW)) {
                deleteJvmWindowsService(new ControlJvmRequest(aJvmId, JvmControlOperation.DELETE_SERVICE), jvm, user);
            }
            jvmPersistenceService.removeJvm(aJvmId);
        } else {
            LOGGER.error("The target JVM {} must be stopped before attempting to delete it", jvm.getJvmName());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "The target JVM must be stopped before attempting to delete it");
        }

    }

    @Override
    public void deleteJvmWindowsService(ControlJvmRequest controlJvmRequest, Jvm jvm, User user) {
        if (!jvm.getState().equals(JvmState.JVM_NEW)) {
            CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmRequest, user);
            final String jvmName = jvm.getJvmName();
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Delete of windows service {} was successful", jvmName);
            } else if (ExecReturnCode.STP_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
                LOGGER.info("No such service found for {} during delete. Continuing with request.", jvmName);
            } else {
                String standardError =
                        commandOutput.getStandardError().isEmpty() ?
                                commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}", jvmName, standardError);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
            }
        }
    }


    protected void addJvmToGroups(final Set<AddJvmToGroupRequest> someAddCommands,
                                  final User anAddingUser) {
        for (final AddJvmToGroupRequest command : someAddCommands) {
            LOGGER.info("Adding jvm {} to group {}", command.getJvmId(), command.getGroupId());
            groupService.addJvmToGroup(command, anAddingUser);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String generateConfigFile(String aJvmName, String templateName) {
        Jvm jvm = jvmPersistenceService.findJvmByExactName(aJvmName);

        final String templateContent = jvmPersistenceService.getJvmTemplate(templateName, jvm.getId());
        if (!templateContent.isEmpty()) {
            return resourceService.generateResourceFile(templateContent, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(aJvmName));
        } else {
            throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Failed to find the template in the database or on the file system");
        }
    }

    @Override
    public Jvm generateAndDeployJvm(String jvmName, User user) {

        Jvm jvm = getJvm(jvmName);

        // only one at a time per JVM
        // TODO return error if .toc directory is already being written to
        // TODO lock on host name (check performance on 125 JVM env)
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

            binaryDistributionService.prepareUnzip(jvm.getHostName());
            binaryDistributionService.distributeJdk(jvm.getHostName());
            binaryDistributionService.distributeTomcat(jvm.getHostName());

            // check for setenv.bat
            checkForSetenvBat(jvm.getJvmName());

            // create the scripts directory if it doesn't exist
            createScriptsDirectory(jvm);

            // copy the invoke and deploy scripts
            deployScriptsToUserTocScriptsDir(jvm, user);

            // delete the service
            // TODO make generic to support multiple OSs
            deleteJvmWindowsService(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), jvm, user);

            // create the tar file
            //
            final String jvmConfigJar = generateJvmConfigJar(jvm);

            // copy the tar file
            secureCopyJvmConfigJar(jvm, jvmConfigJar, user);

            // call script to backup and tar the current directory and
            // then untar the new tar
            deployJvmConfigJar(jvm, user, jvmConfigJar);

            // copy the individual jvm templates to the destination
            deployJvmResourceFiles(jvm, user);

            // deploy any application context xml's in the group
            deployApplicationContextXMLs(jvm, user);

            // re-install the service
            installJvmWindowsService(jvm, user);

            // set the state to stopped
            updateState(jvm.getId(), JvmState.JVM_STOPPED);

        } catch (CommandFailureException | IOException e) {
            LOGGER.error("Failed to generate the JVM config for {} :: ERROR: {}", jvm.getJvmName(), e.getMessage());
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to generate the JVM config: " + e.getMessage(), e);
        } finally {
            jvmWriteLocks.get(jvm.getId().toString()).writeLock().unlock();
        }
        return jvm;
    }

    protected void createScriptsDirectory(Jvm jvm) throws CommandFailureException {
        final String scriptsDir = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();

        final CommandOutput commandOutput = jvmControlService.createDirectory(jvm, scriptsDir);
        ExecReturnCode resultReturnCode = commandOutput.getReturnCode();
        if (!resultReturnCode.wasSuccessful()) {
            LOGGER.error("Creating scripts directory {} FAILED ", scriptsDir);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, commandOutput.getStandardError().isEmpty() ? CommandOutputReturnCode.fromReturnCode(resultReturnCode.getReturnCode()).getDesc() : commandOutput.getStandardError());
        }

    }

    protected void deployScriptsToUserTocScriptsDir(Jvm jvm, User user) throws CommandFailureException, IOException {
        final ControlJvmRequest secureCopyRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY);
        final String commandsScriptsPath = ApplicationProperties.get("commands.scripts-path");

        final String deployConfigJarPath = commandsScriptsPath + "/" + AemControl.Properties.DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME.getValue();
        final String tocScriptsPath = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();
        final String jvmName = jvm.getJvmName();
        final String userId = user.getId();
        final String parentDir = new File(tocScriptsPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");

        createParentDir(jvm, parentDir);
        final String failedToCopyMessage = "Failed to secure copy ";
        final String duringCreationMessage = " during the creation of ";
        if (!jvmControlService.secureCopyFile(secureCopyRequest, deployConfigJarPath, tocScriptsPath, userId).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + deployConfigJarPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
        }

        final String invokeServicePath = commandsScriptsPath + "/" + AemControl.Properties.INVOKE_SERVICE_SCRIPT_NAME.getValue();
        if (!jvmControlService.secureCopyFile(secureCopyRequest, invokeServicePath, tocScriptsPath, userId).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + invokeServicePath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // make sure the scripts are executable
        if (!jvmControlService.changeFileMode(jvm, "a+x", tocScriptsPath, "*.sh").getReturnCode().wasSuccessful()) {
            String message = "Failed to change the file permissions in " + tocScriptsPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
        }
    }

    protected String generateJvmConfigJar(Jvm jvm) throws CommandFailureException {
        String jvmName = jvm.getJvmName();
        LOGGER.info("Generating JVM configuration tar for {}", jvmName);
        final String jvmGeneratedResourcesTempDir = ApplicationProperties.get("paths.generated.resource.dir");
        final String instanceTemplateZipName = ApplicationProperties.get("jwala.instance-template");
        final String binaryDir = ApplicationProperties.get("jwala.binary.dir");
        final String agentDir = ApplicationProperties.get("jwala.agent.dir");

        String jvmResourcesNameDir = jvmGeneratedResourcesTempDir + "/" + jvmName;
        LOGGER.debug("generated resources dir: {}", jvmGeneratedResourcesTempDir);
        LOGGER.debug("generated JVM directory location: {}", jvmResourcesNameDir);

        final File generatedDir = new File("./" + jvmName);
        final File unzipDir = new File(jvmName);
        String configJarPath;
        try {
            final String destDirPath = generatedDir.getAbsolutePath();
            LOGGER.debug("Start Unzip Instance Template {} to {} ", binaryDir+"/"+instanceTemplateZipName, unzipDir);
            // Copy the templates that would be included in the zip file.
            fileManager.unZipFile(new File(binaryDir+"/"+instanceTemplateZipName), unzipDir);
            LOGGER.debug("End Unzip Instance Template {} to {} ", binaryDir+"/"+instanceTemplateZipName, unzipDir);
            final String generatedText = generateInvokeBat(jvmName);
            final String invokeBatDir = generatedDir + "/bin";
            LOGGER.debug("generated invoke.bat text: {}", generatedText);
            LOGGER.debug("generating template in location: {}", invokeBatDir + "/" + CONFIG_FILENAME_INVOKE_BAT);
            createConfigFile(invokeBatDir + "/", CONFIG_FILENAME_INVOKE_BAT, generatedText);
            // copy Agent, JodaTime & JGroup Libraries
            // create the lib directory
            final File generatedJvmDestDirLib = new File(jvmName + "/lib");
            if(!generatedJvmDestDirLib.exists()) {
                generatedJvmDestDirLib.mkdir();
            }
            LOGGER.debug("Copy agent dir: {} to instance template {}", agentDir, generatedJvmDestDirLib);
            File jwalaAgentDir = new File(agentDir);
            if(jwalaAgentDir.exists() && jwalaAgentDir.isDirectory()){
                File[] files  = jwalaAgentDir.listFiles();
                    for(File file:files) {
                        FileUtils.copyFileToDirectory(file, generatedJvmDestDirLib);
                    }
            }
            // copy the start and stop scripts to the instances/jvm/bin directory
            final String commandsScriptsPath = ApplicationProperties.get("commands.scripts-path");
            final File generatedJvmDestDirBin = new File(destDirPath + "/bin");
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);

            // create the logs directory
            createDirectory(destDirPath + "/logs");

            // jar up the file
            String jvmConfigTar = jvmName + CONFIG_JAR;
            JarOutputStream target;
            configJarPath = jvmGeneratedResourcesTempDir + "/" + jvmConfigTar;
            LOGGER.info("Creating JVM jar {}", configJarPath);

            final File jvmResourcesDirFile = new File(jvmGeneratedResourcesTempDir);
            if (!jvmResourcesDirFile.exists() && !jvmResourcesDirFile.mkdir()) {
                LOGGER.error("Could not create {} for JVM resources generation {}", jvmGeneratedResourcesTempDir, jvmName);
                throw new InternalErrorException(AemFaultType.BAD_STREAM, "Could not create " + jvmGeneratedResourcesTempDir + " for JVM resources generation " + jvmName);
            }
            target = new JarOutputStream(new FileOutputStream(configJarPath));
            addFileToTarget(new File("./" + jvmName + "/"), target);
            target.close();
            LOGGER.debug("created resources at {}, copying to {}", generatedDir.getAbsolutePath(), jvmGeneratedResourcesTempDir);
            FileUtils.copyDirectoryToDirectory(generatedDir, jvmResourcesDirFile);
        } catch (IOException e) {
            LOGGER.error("Failed to generate the config jar for {}", jvmName, e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to generate the resources jar for " + jvmName, e);
        } finally {
            try {
                FileUtils.deleteDirectory(generatedDir);
            } catch (IOException e) {
                LOGGER.error("Failed to clean up the staging directory " + generatedDir, e);
            }
        }

        LOGGER.info("Generation of configuration tar SUCCEEDED for {}: {}", jvmName, configJarPath);

        return jvmResourcesNameDir;
    }

    protected void createParentDir(final Jvm jvm, final String parentDir) throws CommandFailureException {
        final CommandOutput commandOutput = jvmControlService.createDirectory(jvm, parentDir);
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("created {} directory successfully", parentDir);
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("create command failed with error trying to create parent directory {} on {} :: ERROR: {}", parentDir, jvm.getHostName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
    }

    protected void secureCopyJvmConfigJar(Jvm jvm, String jvmConfigTar, User user) throws CommandFailureException {
        String configTarName = jvm.getJvmName() + CONFIG_JAR;
        secureCopyFileToJvm(jvm, ApplicationProperties.get("paths.generated.resource.dir") + "/" + configTarName, AemControl.Properties.USER_TOC_SCRIPTS_PATH + "/" + configTarName, user);
        LOGGER.info("Copy of config tar successful: {}", jvmConfigTar);
    }

    protected void deployJvmConfigJar(Jvm jvm, User user, String jvmConfigTar) throws CommandFailureException {
        CommandOutput execData = jvmControlService.controlJvm(
                new ControlJvmRequest(jvm.getId(), JvmControlOperation.DEPLOY_CONFIG_ARCHIVE), user);
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Deployment of config tar was successful: {}", jvmConfigTar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error(
                    "Deploy command completed with error trying to extract and back up JVM config {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc() : standardError);
        }

        // make sure the start/stop scripts are executable
        String instancesDir = ApplicationProperties.get("remote.paths.instances");
        final String targetAbsoluteDir = instancesDir + "/" + jvm.getJvmName() + "/bin";
        if (!jvmControlService.changeFileMode(jvm, "a+x", targetAbsoluteDir, "*.sh").getReturnCode().wasSuccessful()) {
            String message = "Failed to change the file permissions in " + targetAbsoluteDir + " for " + jvm.getJvmName();
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, message);
        }

    }

    protected void deployJvmResourceFiles(Jvm jvm, User user) throws IOException, CommandFailureException {
        final Map<String, String> generatedFiles = generateResourceFiles(jvm.getJvmName());
        if (generatedFiles != null) {
            for (Map.Entry<String, String> entry : generatedFiles.entrySet()) {
                secureCopyFileToJvm(jvm, entry.getKey(), entry.getValue(), user);
            }
        }
    }

    protected void installJvmWindowsService(Jvm jvm, User user) {
        CommandOutput execData = jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(), JvmControlOperation.INVOKE_SERVICE),
                user);
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Invoke of windows service {} was successful", jvm.getJvmName());
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error("Invoking windows service {} failed :: ERROR: {}", jvm.getJvmName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
    }

    protected void createDirectory(String absoluteDirPath) {
        File targetDir = new File(absoluteDirPath);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            LOGGER.error("Bad Stream: Failed to create directory " + absoluteDirPath);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to create directory" + absoluteDirPath);
        }
    }

    /**
     * This method copies a given file to the destination location on the remote machine.
     *
     * @param jvm             Jvm which requires the file
     * @param sourceFile      The source file, which needs to be copied.
     * @param destinationFile The destination file, where the source file should be copied.
     * @param user
     * @throws CommandFailureException If the command fails, this exception contains the details of the failure.
     */
    protected void secureCopyFileToJvm(final Jvm jvm, final String sourceFile, final String destinationFile, User user) throws CommandFailureException {
        final String parentDir = new File(destinationFile).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        createParentDir(jvm, parentDir);
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY);
        final CommandOutput commandOutput = jvmControlService.secureCopyFile(controlJvmRequest, sourceFile, destinationFile, user.getId());
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully copied {} to destination location {} on {}", sourceFile, destinationFile, jvm.getHostName());
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("Copy command failed with error trying to copy file {} to {} :: ERROR: {}", sourceFile, jvm.getHostName(), standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
    }

    protected void addFileToTarget(File source, JarOutputStream target) throws IOException {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile : source.listFiles()) {
                    addFileToTarget(nestedFile, target);
                }
                return;
            }

            JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    public Jvm generateAndDeployFile(String jvmName, String fileName, User user) {
        Jvm jvm = getJvm(jvmName);

        // only one at a time per jvm
        if (!jvmWriteLocks.containsKey(jvm.getId().getId().toString())) {
            jvmWriteLocks.put(jvm.getId().getId().toString(), new ReentrantReadWriteLock());
        }
        jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().lock();

        final String badStreamMessage = "Bad Stream: ";
        try {
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM {} must be stopped before attempting to update the resource files", jvm.getJvmName());
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }

            final String metaDataPath;
            String metaDataStr = getResourceTemplateMetaData(jvmName, fileName);
            ResourceTemplateMetaData resourceTemplateMetaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
            metaDataPath = resourceTemplateMetaData.getDeployPath();
            String resourceSourceCopy;
            String resourceDestPath = ResourceFileGenerator.generateResourceConfig(metaDataPath, resourceService.generateResourceGroup(), jvm) + "/" + fileName;
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                resourceSourceCopy = getResourceTemplate(jvmName, fileName, false);
            } else {
                String fileContent = generateConfigFile(jvmName, fileName);
                String jvmResourcesNameDir = ApplicationProperties.get("paths.generated.resource.dir") + "/" + jvmName;
                resourceSourceCopy = jvmResourcesNameDir + "/" + fileName;
                createConfigFile(jvmResourcesNameDir + "/", fileName, fileContent);
            }

            deployJvmConfigFile(fileName, jvm, resourceDestPath, resourceSourceCopy, user);
        } catch (IOException e) {
            String message = "Failed to write file";
            LOGGER.error(badStreamMessage + message, e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, message, e);
        } catch (CommandFailureException ce) {
            String message = "Failed to copy file";
            LOGGER.error(badStreamMessage + message, ce);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, message, ce);
        } finally {
            jvmWriteLocks.get(jvm.getId().getId().toString()).writeLock().unlock();
        }

        return jvm;
    }

    protected void deployJvmConfigFile(String fileName, Jvm jvm, String destPath, String sourcePath, User user)
            throws CommandFailureException {
        final String parentDir = new File(destPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        createParentDir(jvm, parentDir);
        CommandOutput result =
                jvmControlService.secureCopyFile(new ControlJvmRequest(jvm.getId(), JvmControlOperation.SECURE_COPY), sourcePath, destPath, user.getId());
        if (result.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successful generation and deploy of {} to {}", fileName, jvm.getJvmName());
        } else {
            String standardError =
                    result.getStandardError().isEmpty() ? result.getStandardOutput() : result.getStandardError();
            LOGGER.error("Copying config file {} failed :: ERROR: {}", fileName, standardError);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(result.getReturnCode().getReturnCode()).getDesc());
        }
    }


    @Override
    @Transactional
    public String performDiagnosis(Identifier<Jvm> aJvmId) {
        // if the Jvm does not exist, we'll get a 404 NotFoundException
        Jvm jvm = jvmPersistenceService.getJvm(aJvmId);

        pingAndUpdateJvmState(jvm);

        SimpleTemplateEngine engine = new SimpleTemplateEngine();
        Map<String, Object> binding = new HashMap<>();
        binding.put("jvm", jvm);

        try {
            return engine.createTemplate(DIAGNOSIS_INITIATED).make(binding).toString();
        } catch (CompilationFailedException | ClassNotFoundException | IOException e) {
            throw new ApplicationException(DIAGNOSIS_INITIATED, e);
            // why do this? Because if there was a problem with the template that made
            // it past initial testing, then it is probably due to the jvm in the binding
            // so just dump out the diagnosis template and the exception so it can be
            // debugged.
        }
    }

    @Override
    public void checkForSetenvBat(String jvmName) {
        try {
            jvmPersistenceService.getResourceTemplate(jvmName, "setenv.bat");
        } catch (NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("No setenv.bat configured for JVM: {}", jvmName, e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, "No setenv.bat template found for " + jvmName + ". Unable to continue processing.");
        }
        LOGGER.debug("Found setenv.bat for JVM: {}. Continuing with process. ", jvmName);
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param jvm   the jvm
     * @param state {@link JvmState}
     * @param msg   a message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setState(final Jvm jvm,
                          final JvmState state,
                          final String msg) {
        jvmPersistenceService.updateState(jvm.getId(), state, msg);
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM));
        groupStateNotificationService.retrieveStateAndSendToATopic(jvm.getId(), Jvm.class);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String jvmName, String groupName, String template) {
        return resourceService.generateResourceFile(template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvm(jvmName, groupName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmPersistenceService.getResourceTemplateNames(jvmName);
    }

    @Override
    @Transactional
    public String getResourceTemplate(final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = jvmPersistenceService.getResourceTemplate(jvmName, resourceTemplateName);
        if (tokensReplaced) {
            return resourceService.generateResourceFile(template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(jvmName));
        }
        return template;
    }

    @Override
    public String getResourceTemplateMetaData(String jvmName, String fileName) {
        return jvmPersistenceService.getResourceTemplateMetaData(jvmName, fileName);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        String retVal = null;
        try {
            retVal = jvmPersistenceService.updateResourceTemplate(jvmName, resourceTemplateName, template);
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
        }
        return retVal;
    }

    @Override
    @Transactional
    public String generateInvokeBat(String jvmName) {
        final String invokeBatTemplateContent = fileManager.getResourceTypeTemplate("InvokeBat");
        return resourceService.generateResourceFile(invokeBatTemplateContent, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(jvmName));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmPersistenceService.updateState(id, state, "");
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
    }

    @Override
    @Transactional
    public void pingAndUpdateJvmState(final Jvm jvm) {
        ClientHttpResponse response = null;
        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.info(">>> Response = {} from jvm {}", response.getStatusCode(), jvm.getId().getId());
            if (response.getStatusCode() == HttpStatus.OK) {
                setState(jvm, JvmState.JVM_STARTED, StringUtils.EMPTY);
            } else {
                setState(jvm, JvmState.JVM_STOPPED,
                        "Request for '" + jvm.getStatusUri() + "' failed with a response code of '" +
                                response.getStatusCode() + "'");
            }
        } catch (IOException ioe) {
            LOGGER.info(ioe.getMessage(), ioe);
            setState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
        } catch (RuntimeException rte) {
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    @Transactional
    public void deployApplicationContextXMLs(Jvm jvm, User user) {
        List<Group> groupList = jvmPersistenceService.findGroupsByJvm(jvm.getId());
        for (Group group : groupList) {
            for (Application app : applicationService.findApplications(group.getId())) {
                for (String templateName : applicationService.getResourceTemplateNames(app.getName(), jvm.getJvmName())) {
                    LOGGER.info("Deploying application xml {} for JVM {} in group {}", templateName, jvm.getJvmName(), group.getName());
                    applicationService.deployConf(app.getName(), group.getName(), jvm.getJvmName(), templateName, resourceService.generateResourceGroup(), user);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmStartedCount(final String groupName) {
        return jvmPersistenceService.getJvmStartedCount(groupName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmCount(final String groupName) {
        return jvmPersistenceService.getJvmCount(groupName);
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmStoppedCount(groupName);
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmForciblyStoppedCount(groupName);
    }

    @Override
    public Map<String, String> generateResourceFiles(final String jvmName) throws IOException {
        Map<String, String> generatedFiles = null;
        final List<JpaJvmConfigTemplate> jpaJvmConfigTemplateList = jvmPersistenceService.getConfigTemplates(jvmName);
        final ObjectMapper mapper = new ObjectMapper();
        for (final JpaJvmConfigTemplate jpaJvmConfigTemplate : jpaJvmConfigTemplateList) {
            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            final String resourceTemplateMetaDataString = resourceService.generateResourceFile(jpaJvmConfigTemplate.getMetaData(), resourceGroup, jvm);
            final ResourceTemplateMetaData resourceTemplateMetaData =
                    mapper.readValue(resourceTemplateMetaDataString, ResourceTemplateMetaData.class);
            if (generatedFiles == null) {
                generatedFiles = new HashMap<>();
            }
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                if (generatedFiles == null) {
                    generatedFiles = new HashMap<>();
                }
                generatedFiles.put(jpaJvmConfigTemplate.getTemplateContent(),
                        resourceTemplateMetaData.getDeployPath() + "/" + resourceTemplateMetaData.getDeployFileName());
            } else {
                final String generatedResourceStr = resourceService.generateResourceFile(jpaJvmConfigTemplate.getTemplateContent(),
                        resourceGroup, jvm);
                generatedFiles.put(createConfigFile(ApplicationProperties.get("paths.generated.resource.dir") + "/" + jvmName, resourceTemplateMetaData.getDeployFileName(), generatedResourceStr),
                        resourceTemplateMetaData.getDeployPath() + "/" + resourceTemplateMetaData.getDeployFileName());
            }
        }
        return generatedFiles;
    }

    /**
     * This method creates a temp file .tpl file, with the generatedResourceString as the input data for the file.
     *
     * @param generatedResourcesTempDir
     * @param configFileName            The file name that apprears at the destination.
     * @param generatedResourceString   The contents of the file.
     * @return the location of the newly created temp file
     * @throws IOException
     */
    protected String createConfigFile(String generatedResourcesTempDir, final String configFileName, String generatedResourceString) throws IOException {
        File templateFile = new File(generatedResourcesTempDir + "/" + configFileName);
        if (configFileName.endsWith(".bat")) {
            generatedResourceString = generatedResourceString.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(templateFile, generatedResourceString);
        return templateFile.getAbsolutePath();
    }

    @Override
    @Transactional
    public void deleteJvm(final String name, final String userName) {
        final Jvm jvm = getJvm(name);
        if (!jvm.getState().isStartedState()) {
            LOGGER.info("Removing JVM from the database and deleting the service for jvm {}", name);
            if (!jvm.getState().equals(JvmState.JVM_NEW)) {
                deleteJvmWindowsService(new ControlJvmRequest(jvm.getId(), JvmControlOperation.DELETE_SERVICE), jvm,
                        new User(userName));
            }
            jvmPersistenceService.removeJvm(jvm.getId());
        } else {
            LOGGER.error("The target JVM {} must be stopped before attempting to delete it", jvm.getJvmName());
            throw new JvmServiceException("The target JVM must be stopped before attempting to delete it");
        }
    }
}
