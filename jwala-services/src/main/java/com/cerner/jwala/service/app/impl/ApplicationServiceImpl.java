package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.control.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private final ExecutorService executorService;
    final String JWALA_SCRIPTS_PATH = ApplicationProperties.get("remote.commands.user-scripts");

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    private final ResourceService resourceService;

    private Map<String, ReentrantReadWriteLock> writeLock = new HashMap<>();

    private final RemoteCommandExecutorImpl remoteCommandExecutor;

    private final BinaryDistributionService binaryDistributionService;

    private GroupService groupService;

    private final HistoryFacadeService historyFacadeService;

    private static final String UNZIP_EXE = "unzip.exe";

    public ApplicationServiceImpl(final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final GroupService groupService,
                                  final ResourceService resourceService,
                                  final RemoteCommandExecutorImpl remoteCommandExecutor,
                                  final BinaryDistributionService binaryDistributionService,
                                  final HistoryFacadeService historyFacadeService) {
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.groupService = groupService;
        this.historyFacadeService = historyFacadeService;
        this.resourceService = resourceService;
        this.remoteCommandExecutor = remoteCommandExecutor;
        this.binaryDistributionService = binaryDistributionService;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("resources.thread-task-executor.pool.size", "25")));
    }


    @Transactional(readOnly = true)
    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationPersistenceService.getApplication(aApplicationId);
    }

    @Transactional(readOnly = true)
    @Override
    public Application getApplication(final String name) {
        return applicationPersistenceService.getApplication(name);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> getApplications() {
        return applicationPersistenceService.getApplications();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplications(Identifier<Group> groupId) {
        return applicationPersistenceService.findApplicationsBelongingTo(groupId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId) {
        return applicationPersistenceService.findApplicationsBelongingToJvm(jvmId);
    }

    @Transactional
    @Override
    public Application updateApplication(UpdateApplicationRequest updateApplicationRequest, User anUpdatingUser) {
        updateApplicationRequest.validate();

        return applicationPersistenceService.updateApplication(updateApplicationRequest);
    }

    @Transactional
    @Override
    public Application createApplication(final CreateApplicationRequest createApplicationRequest,
                                         final User aCreatingUser) {

        createApplicationRequest.validate();

        return applicationPersistenceService.createApplication(createApplicationRequest);
    }

    @Transactional
    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        applicationPersistenceService.removeApplication(anAppIdToRemove);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String appName, final String jvmName) {
        return applicationPersistenceService.getResourceTemplateNames(appName, jvmName);
    }

    @Override
    @Transactional
    public String getResourceTemplate(final String appName,
                                      final String groupName,
                                      final String jvmName,
                                      final String resourceTemplateName,
                                      final ResourceGroup resourceGroup,
                                      final boolean tokensReplaced) {
        final String template = applicationPersistenceService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
        if (tokensReplaced) {
            final Application application = applicationPersistenceService.findApplication(appName, groupName, jvmName);
            application.setParentJvm(jvmPersistenceService.findJvmByExactName(jvmName));
            return resourceService.generateResourceFile(resourceTemplateName, template, resourceGroup, application, ResourceGeneratorType.TEMPLATE);
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName) {
        return applicationPersistenceService.updateResourceTemplate(appName, resourceTemplateName, template, jvmName, groupName);
    }

    @Override
    @Transactional
    // TODO: Have an option to do a hot deploy or not.
    public CommandOutput deployConf(final String appName, final String groupName, final String jvmName,
                                    final String resourceTemplateName, ResourceGroup resourceGroup, User user) {
        final StringBuilder key = new StringBuilder();
        key.append(groupName).append(jvmName).append(appName).append(resourceTemplateName);
        try {
            // only one at a time
            if (!writeLock.containsKey(key.toString())) {
                writeLock.put(key.toString(), new ReentrantReadWriteLock());
            }
            writeLock.get(key.toString()).writeLock().lock();
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setResourceName(resourceTemplateName)
                    .setGroupName(groupName)
                    .setWebAppName(appName)
                    .setJvmName(jvmName)
                    .build();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM must be stopped before attempting to update the resource files");
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }
            final String hostName = jvm.getHostName();
            return resourceService.generateAndDeployFile(resourceIdentifier, appName, resourceTemplateName, hostName);
        } catch (ResourceFileGeneratorException e) {
            LOGGER.error("Fail to generate the resource file {}", resourceTemplateName, e);
            throw new DeployApplicationConfException(e);
        } finally {
            writeLock.get(key.toString()).writeLock().unlock();
        }
    }

    @Override
    @Transactional
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest) {
        uploadAppTemplateRequest.validate();
        Jvm jvm = jvmPersistenceService.findJvmByExactName(uploadAppTemplateRequest.getJvmName());
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
        return applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String fileName, String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup) {
        final Application application;
        if (StringUtils.isNotEmpty(jvmName)) {
            application = applicationPersistenceService.findApplication(appName, groupName, jvmName);
            application.setParentJvm(jvmPersistenceService.findJvmByExactName(jvmName));
        } else {
            application = applicationPersistenceService.getApplication(appName);
        }
        return resourceService.generateResourceFile(fileName, template, resourceGroup, application, ResourceGeneratorType.PREVIEW);
    }

    @Override
    @Transactional
    public void copyApplicationWarToGroupHosts(Application application) {
        Group group = groupService.getGroup(application.getGroup().getId());
        final Set<Jvm> theJvms = group.getJvms();
        if (theJvms != null && !theJvms.isEmpty()) {
            Set<String> hostNames = new HashSet<>();
            for (Jvm jvm : theJvms) {
                final String host = jvm.getHostName().toLowerCase(Locale.US);
                if (!hostNames.contains(host)) {
                    hostNames.add(host);
                }
            }
            copyAndExecuteCommand(application, hostNames);
        }
    }

    @Override
    @Transactional
    public void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup) {
        List<String> appResourcesNames = groupService.getGroupAppsResourceTemplateNames(groupName);
        Group group = groupService.getGroup(app.getGroup().getId());
        final Set<Jvm> jvms = group.getJvms();
        if (null != appResourcesNames && !appResourcesNames.isEmpty()) {
            for (String resourceTemplateName : appResourcesNames) {
                String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
                try {
                    ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(resourceTemplateName, app, metaDataStr);
                    if (jvms != null && !jvms.isEmpty() && !metaData.getEntity().getDeployToJvms()) {
                        // still need to iterate through the JVMs to get the host names
                        Set<String> hostNames = new HashSet<>();
                        for (Jvm jvm : jvms) {
                            final String host = jvm.getHostName().toLowerCase(Locale.US);
                            if (!hostNames.contains(host)) {
                                hostNames.add(host);
                                groupService.deployGroupAppTemplate(groupName, resourceTemplateName, app, jvm);
                            }
                        }

                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to map meta data for template {} in group {}", resourceTemplateName, groupName, e);
                    throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to read meta data for template " + resourceTemplateName + " in group " + groupName, e);
                }
            }
        }

    }

    @Override
    @Transactional
    public void copyApplicationWarToHost(Application application, String hostName) {
        if (hostName != null && !hostName.isEmpty()) {
            Set<String> hostNames = new HashSet<>();
            hostNames.add(hostName);
            copyAndExecuteCommand(application, hostNames);
        }
    }

    private void copyAndExecuteCommand(Application application, Set<String> hostNames) {
        File applicationWar = new File(application.getWarPath());
        final String sourcePath = applicationWar.getParent();
        File tempWarFile = new File(sourcePath + "/" + application.getWarName());
        Map<String, Future<CommandOutput>> futures = new HashMap<>();
        try {
            FileCopyUtils.copy(applicationWar, tempWarFile);
            final String destPath = ApplicationProperties.get("remote.jwala.webapps.dir");
            for (String hostName : hostNames) {
                Future<CommandOutput> commandOutputFuture = executeCopyCommand(application, tempWarFile, destPath, null, hostName);
                futures.put(hostName, commandOutputFuture);
            }
            for (Entry<String, Future<CommandOutput>> entry : futures.entrySet()) {
                CommandOutput execData = entry.getValue().get();
                if (execData.getReturnCode().wasSuccessful()) {
                    LOGGER.info("Copy of application war {} to {} was successful", applicationWar.getName(), entry.getKey());
                } else {
                    String errorOutput = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                    LOGGER.error("Copy of application war {} to {} FAILED::{}", applicationWar.getName(), entry.getKey(), errorOutput);
                    throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Failed to copy application war to the group host " + entry.getKey());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Creation of temporary war file for {} FAILED :: {}", application.getWarPath(), e);
            throw new InternalErrorException(FaultType.INVALID_PATH, "Failed to create temporary war file for copying to remote hosts");
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("FAILURE getting return status from copying web app war", e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Exception thrown while copying war", e);
        } finally {
            if (tempWarFile.exists()) {
                tempWarFile.delete();
            }
        }
    }

    protected Future<CommandOutput> executeCopyCommand(final Application application, final File tempWarFile, final String destPath, final Jvm jvm, final String host) {
        final String name = application.getName();
        Future<CommandOutput> commandOutputFuture = executorService.submit(new Callable<CommandOutput>() {
            @Override
            public CommandOutput call() throws Exception {
                final String parentDir = new File(destPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
                CommandOutput commandOutput = executeCreateDirectoryCommand(null, host, parentDir);
                if (commandOutput.getReturnCode().wasSuccessful()) {
                    LOGGER.info("Successfully created parent dir {} on host {}", parentDir, host);
                } else {
                    final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                    LOGGER.error("Error in creating parent dir {} on host {}:: ERROR : {}", parentDir, host, standardError);
                    throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                }
                LOGGER.info("Copying {} war to host {}", name, host);
                commandOutput = executeSecureCopyCommand(null, host, tempWarFile.getAbsolutePath().replaceAll("\\\\", "/"), destPath);

                if (application.isUnpackWar()) {
                    final String warName = application.getWarName();
                    LOGGER.info("Unpacking war {} on host {}", warName, host);

                    // create the .jwala directory as the destination for the unpack-war script
                    commandOutput = executeCreateDirectoryCommand(null, host, JWALA_SCRIPTS_PATH);
                    if (commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.info("Successfully created the parent dir {} on host", JWALA_SCRIPTS_PATH, host);
                    } else {
                        final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                        LOGGER.error("Error in creating parent dir {} on host {}:: ERROR : {}", JWALA_SCRIPTS_PATH, host, standardError);
                        throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                    }

                    final String unpackWarScriptPath = ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.UNPACK_BINARY_SCRIPT_NAME;
                    final String destinationUnpackWarScriptPath = JWALA_SCRIPTS_PATH + "/" + AemControl.Properties.UNPACK_BINARY_SCRIPT_NAME;
                    commandOutput = executeSecureCopyCommand(null, host, unpackWarScriptPath, destinationUnpackWarScriptPath);

                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.error("Error in copying the " + unpackWarScriptPath + " to " + destinationUnpackWarScriptPath + " on " + host);
                        return commandOutput; // return immediately if the copy failed
                    }

                    // make sure the scripts are executable
                    commandOutput = executeChangeFileModeCommand(null, host, "a+x", JWALA_SCRIPTS_PATH, "*.sh");
                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.error("Error in changing file permissions on " + JWALA_SCRIPTS_PATH + " on host:" + host);
                        return commandOutput;
                    }

                    binaryDistributionService.prepareUnzip(host);

                    final String zipDestinationOption = FilenameUtils.removeExtension(destPath);

                    LOGGER.debug("Checking if previously unpacked: {}", zipDestinationOption);
                    commandOutput = executeCheckIfFileExistsCommand(null, host, zipDestinationOption);

                    if (commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.debug("unpacked directory found at {}, backing it up", zipDestinationOption);
                        commandOutput = executeBackUpCommand(null, host, zipDestinationOption);

                        if (commandOutput.getReturnCode().wasSuccessful()) {
                            LOGGER.debug("successful back up of {}", zipDestinationOption);
                        } else {
                            final String standardError = "Could not back up " + zipDestinationOption;
                            LOGGER.error(standardError);
                            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                        }
                    }

                    commandOutput = executeUnzipBinaryCommand(null, host, destPath, zipDestinationOption, "");
                }
                return commandOutput;
            }
        });
        return commandOutputFuture;
    }

    @Override
    public CommandOutput executeBackUpCommand(final String entity, final String host, final String source) throws CommandFailureException {
        final String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
        final String destination = source + currentDateSuffix;
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                ApplicationControlOperation.BACK_UP,
                new WindowsApplicationPlatformCommandProvider(),
                source,
                destination
        );
    }

    @Override
    public CommandOutput executeCreateDirectoryCommand(final String entity, final String host, final String directoryName) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                ApplicationControlOperation.CREATE_DIRECTORY,
                new WindowsApplicationPlatformCommandProvider(),
                directoryName
        );
    }

    @Override
    public CommandOutput executeSecureCopyCommand(final String entity, final String host, final String source, final String destination) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                ApplicationControlOperation.SECURE_COPY,
                new WindowsApplicationPlatformCommandProvider(),
                source,
                destination
        );
    }

    @Override
    public CommandOutput executeCheckIfFileExistsCommand(final String entity, final String host, final String fileName) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                ApplicationControlOperation.CHECK_FILE_EXISTS,
                new WindowsApplicationPlatformCommandProvider(),
                fileName
        );
    }

    @Override
    public CommandOutput executeChangeFileModeCommand(final String entity, final String host, final String mode, final String fileName, final String fileOptions) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                ApplicationControlOperation.CHANGE_FILE_MODE,
                new WindowsApplicationPlatformCommandProvider(),
                mode,
                fileName,
                fileOptions
        );
    }

    @Override
    public CommandOutput executeUnzipBinaryCommand(final String entity, final String host, final String fileName, final String destination, final String options) throws CommandFailureException {
        return remoteCommandExecutor.executeRemoteCommand(
                entity,
                host,
                BinaryDistributionControlOperation.UNZIP_BINARY,
                new WindowsBinaryDistributionPlatformCommandProvider(),
                ApplicationProperties.get("remote.commands.user-scripts") + "/" + UNZIP_EXE,
                fileName,
                destination,
                options
        );
    }

    @Override
    public void deployConf(final String appName, final String hostName, final User user) {
        final Application application = applicationPersistenceService.getApplication(appName);
        final Group group = groupService.getGroup(application.getGroup().getId());
        final List<String> hostNames = getDeployHostList(hostName, group, application);

        LOGGER.info("deploying templates to hosts: {}", hostNames.toString());
        historyFacadeService.write("", group, "Deploy \"" + appName + "\" resources", EventType.USER_ACTION_INFO, user.getId());

        checkForRunningJvms(group, hostNames, user);

        validateApplicationResources(appName, group);

        final Set<String> resourceSet = getWebAppOnlyResources(group, appName);

        final List<String> keys = getKeysAndAcquireWriteLock(appName, hostNames);

        try {
            final Map<String, Future<Set<CommandOutput>>> futures = deployApplicationResourcesForHosts(hostNames, group, application, resourceSet);
            waitForDeploy(appName, futures);
        } finally {
            releaseWriteLocks(keys);
        }
    }

    private void validateApplicationResources(String appName, Group group) {
        final String groupName = group.getName();
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("*")
                .setGroupName(groupName)
                .setWebAppName(appName)
                .build();
        resourceService.validateAllResourcesForGeneration(resourceIdentifier);
        LOGGER.info("Application {} in group {} passed resource validation", appName, groupName);
    }

    protected List<String> getDeployHostList(final String hostName, final Group group, final Application application) {
        final String groupName = group.getName();
        final String appName = application.getName();
        final List<String> hostNames = new ArrayList<>();
        final List<String> allHosts = groupService.getHosts(groupName);
        if (allHosts == null || allHosts.isEmpty()) {
            LOGGER.error("No hosts found for the group: {} and application: {}", groupName, appName);
            throw new InternalErrorException(FaultType.GROUP_MISSING_HOSTS, "No host found for the application " + appName);
        }
        if (hostName == null || hostName.isEmpty()) {
            LOGGER.info("Hostname not passed, deploying to all hosts");
            for (String host : allHosts) {
                hostNames.add(host.toLowerCase(Locale.US));
            }
        } else {
            LOGGER.info("host name provided {}", hostName);
            for (final String host : allHosts) {
                if (hostName.toLowerCase(Locale.US).equals(host.toLowerCase(Locale.US))) {
                    hostNames.add(host.toLowerCase(Locale.US));
                }
            }
            if (hostNames.isEmpty()) {
                LOGGER.error("Hostname {} does not belong to the group {}", hostName, groupName);
                throw new InternalErrorException(FaultType.INVALID_HOST_NAME, "The hostname: " + hostName + " does not belong to the group " + groupName);
            }
        }
        return hostNames;
    }

    // TODO: See if we can implement method chaining for resource deployment which can be used by other resource related service
    protected void checkForRunningJvms(final Group group, final List<String> hostNames, final User user) {
        final List<Jvm> runningJvmList = new ArrayList<>();
        final List<String> runningJvmNameList = new ArrayList<>();
        for (final Jvm jvm : group.getJvms()) {
            if (hostNames.contains(jvm.getHostName().toLowerCase(Locale.US)) && jvm.getState().isStartedState()) {
                runningJvmList.add(jvm);
                runningJvmNameList.add(jvm.getJvmName());
            }
        }

        if (!runningJvmList.isEmpty()) {
            final String errMsg = "Make sure the following JVMs are completely stopped before deploying.";
            LOGGER.error(errMsg + " {}", runningJvmNameList);
            for (final Jvm jvm : runningJvmList) {
                historyFacadeService.write(Jvm.class.getSimpleName() + " " + jvm.getJvmName(), jvm.getGroups(),
                        "Web app resource(s) cannot be deployed on a running JVM!",
                        EventType.SYSTEM_ERROR, user.getId());
            }
            throw new ApplicationServiceException(FaultType.RESOURCE_DEPLOY_FAILURE, errMsg, runningJvmNameList);
        }
    }

    protected Set<String> getWebAppOnlyResources(final Group group, String appName) {
        final String groupName = group.getName();
        final Set<String> resourceSet = new HashSet<>();
        List<String> resourceTemplates = groupService.getGroupAppsResourceTemplateNames(groupName, appName);
        for (String resourceTemplate : resourceTemplates) {
            String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplate);
            LOGGER.debug("metadata for template: {} is {}", resourceTemplate, metaDataStr);
            try {
                ResourceTemplateMetaData metaData = resourceService.getMetaData(metaDataStr);
                if (!metaData.getEntity().getDeployToJvms()) {
                    LOGGER.info("Template {} needs to be deployed adding it to the list", resourceTemplate);
                    resourceSet.add(resourceTemplate);
                } else {
                    LOGGER.info("Not deploying {} because deployToJvms=true", resourceTemplate);
                }
            } catch (IOException e) {
                LOGGER.error("Error in templatizing the metadata file", e);
                throw new InternalErrorException(FaultType.IO_EXCEPTION, "Error in templatizing the metadata for resource " + resourceTemplate);
            }
        }
        return resourceSet;
    }

    protected Map<String, Future<Set<CommandOutput>>> deployApplicationResourcesForHosts(
            final List<String> hostNames, final Group group, final Application application, final Set<String> resourceSet) {
        final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Map<String, Future<Set<CommandOutput>>> futures = new HashMap<>();
        for (final String host : hostNames) {
            Future<Set<CommandOutput>> commandOutputFutureSet = executorService.submit
                    (new Callable<Set<CommandOutput>>() {
                         @Override
                         public Set<CommandOutput> call() throws Exception {
                             Set<CommandOutput> commandOutputs = new HashSet<>();
                             SecurityContextHolder.getContext().setAuthentication(authentication);
                             for (final String resource : resourceSet) {
                                 LOGGER.info("Deploying {} to host {}", resource, host);
                                 commandOutputs.add(groupService.deployGroupAppTemplate(group.getName(), resource, application, host));
                             }
                             return commandOutputs;
                         }
                     }
                    );
            futures.put(host, commandOutputFutureSet);
        }
        return futures;
    }

    protected void waitForDeploy(final String appName, final Map<String, Future<Set<CommandOutput>>> futures) {
        if (futures != null) {
            for (Entry<String, Future<Set<CommandOutput>>> entry : futures.entrySet()) {
                try {
                    long timeout = Long.parseLong(ApplicationProperties.get("remote.jwala.execution.timeout.seconds", "600"));
                    Set<CommandOutput> commandOutputSet = entry.getValue().get(timeout, TimeUnit.SECONDS);
                    for (CommandOutput commandOutput : commandOutputSet) {
                        if (!commandOutput.getReturnCode().wasSuccessful()) {
                            final String errorMessage = "Error in deploying resources to host " + entry.getKey() +
                                    " for application " + appName;
                            LOGGER.error(errorMessage);
                            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, errorMessage);
                        }
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.error("Error in executing deploy", e);
                    throw new InternalErrorException(FaultType.RESOURCE_DEPLOY_FAILURE, e.getMessage());
                }
            }
        }
    }

    protected List<String> getKeysAndAcquireWriteLock(String appName, List<String> hostNames) {
        List<String> keys = new ArrayList<>();
        for (final String host : hostNames) {
            final String key = appName + "/" + host;
            if (!writeLock.containsKey(key)) {
                writeLock.put(key, new ReentrantReadWriteLock());
            }
            boolean gotLock = writeLock.get(key).writeLock().tryLock();
            if (!gotLock) {
                LOGGER.error("Could not get lock for host: {} and app: {}", host, appName);
                releaseWriteLocks(keys);
                throw new InternalErrorException(FaultType.SERVICE_EXCEPTION, "Current resource is being deployed, wait for deploy to complete.");
            } else {
                keys.add(key);
            }
        }
        return keys;
    }

    protected void releaseWriteLocks(List<String> keys) {
        for (String key : keys) {
            if (writeLock.containsKey(key) && writeLock.get(key).isWriteLocked()) {
                writeLock.get(key).writeLock().unlock();
            }
        }
    }
}
