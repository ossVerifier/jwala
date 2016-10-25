package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.message.JvmHistoryEvent;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.*;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.control.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.control.jvm.command.windows.WindowsJvmNetOperation;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.WebArchiveManager;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.app.PrivateApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final String GENERATED_RESOURCE_DIR = "paths.generated.resource.dir";
    private static final String JWALA_WEBAPPS_DIR = "remote.jwala.webapps.dir";
    private final ExecutorService executorService;
    final String JWALA_SCRIPTS_PATH = ApplicationProperties.get("remote.commands.user-scripts");

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private WebArchiveManager webArchiveManager;

    @Autowired
    private PrivateApplicationService privateApplicationService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    private final ResourceService resourceService;

    private RemoteCommandExecutor<ApplicationControlOperation> applicationCommandExecutor;

    private Map<String, ReentrantReadWriteLock> writeLock = new HashMap<>();

    private final RemoteCommandExecutorImpl remoteCommandExecutor;

    private final BinaryDistributionService binaryDistributionService;

    private GroupService groupService;
    private final HistoryService historyService;
    private final MessagingService messagingService;
    private static final String UNZIP_EXE = "unzip.exe";

    public ApplicationServiceImpl(final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final RemoteCommandExecutor<ApplicationControlOperation> applicationCommandService,
                                  final GroupService groupService,
                                  final WebArchiveManager webArchiveManager,
                                  final PrivateApplicationService privateApplicationService,
                                  final HistoryService historyService,
                                  final MessagingService messagingService,
                                  final ResourceService resourceService,
                                  final RemoteCommandExecutorImpl remoteCommandExecutor,
                                  final BinaryDistributionService binaryDistributionService) {
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationCommandExecutor = applicationCommandService;
        this.groupService = groupService;
        this.webArchiveManager = webArchiveManager;
        this.privateApplicationService = privateApplicationService;
        this.historyService = historyService;
        this.messagingService = messagingService;
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

    /**
     * Non-transactional entry point, utilizes {@link PrivateApplicationServiceImpl}
     */
    @Override
    public Application uploadWebArchive(UploadWebArchiveRequest uploadWebArchiveRequest, User user) {
        uploadWebArchiveRequest.validate();
        return privateApplicationService.uploadWebArchiveUpdateDB(uploadWebArchiveRequest, privateApplicationService.uploadWebArchiveData(uploadWebArchiveRequest));
    }

    @Override
    @Transactional
    public Application deleteWebArchive(final Identifier<Application> appId, final User user) {
        final Application app = applicationPersistenceService.getApplication(appId);

        final List<String> resourceNames = new ArrayList<>();
        resourceNames.add(app.getWarName());
        resourceService.deleteGroupLevelAppResources(app.getName(), app.getGroup().getName(), resourceNames);

        final Application updatedApp = applicationPersistenceService.deleteWarInfo(app.getName());

        final RemoveWebArchiveRequest removeWarRequest = new RemoveWebArchiveRequest(app);
        try {
            final RepositoryFileInformation result = webArchiveManager.remove(removeWarRequest);
            LOGGER.info("Archive Delete: " + result.toString());
            if (result.getType() != RepositoryFileInformation.Type.DELETED) {
                // If the physical file can't be deleted for some reason don't throw an exception since there's no
                // outright ill effect if the war file is not removed in the file system.
                // The said file might not exist anymore also which is the reason for the error.
                LOGGER.error("Failed to delete the archive {}! WebArchiveManager remove result type = {}" , app.getWarPath(),
                        result.getType());
            }
        } catch (final IOException ioe) {
            // If the physical file can't be deleted for some reason don't throw an exception since there's no
            // outright ill effect if the war file is not removed in the file system.
            LOGGER.error("Failed to delete the archive {}!" , app.getWarPath(), ioe);
        }

        return updatedApp;
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

            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM must be stopped before attempting to update the resource files");
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }

            final File confFile = createConfFile(appName, groupName, jvmName, resourceTemplateName, resourceGroup);
            final Application app = applicationPersistenceService.findApplication(appName, groupName, jvmName);

            String metaData = applicationPersistenceService.getMetaData(appName, jvmName, groupName, resourceTemplateName);
            app.setParentJvm(jvm);
            ResourceTemplateMetaData templateMetaData = resourceService.getTokenizedMetaData(resourceTemplateName, app, metaData);
            final String deployFileName = templateMetaData.getDeployFileName();
            final String destPath = templateMetaData.getDeployPath() + '/' + deployFileName;
            String srcPath;
            if (templateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                srcPath = applicationPersistenceService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
            } else {
                srcPath = confFile.getAbsolutePath().replace("\\", "/");
            }

            final String eventDescription = WindowsJvmNetOperation.SECURE_COPY.name() + " " + deployFileName;
            final String id = user.getId();

            historyService.createHistory("JVM " + jvm.getJvmName(), new ArrayList<Group>(jvm.getGroups()), eventDescription, EventType.USER_ACTION, id);
            messagingService.send(new JvmHistoryEvent(jvm.getId(), eventDescription, id, DateTime.now(), JvmControlOperation.SECURE_COPY));

            final String deployJvmName = jvm.getJvmName();
            final String hostName = jvm.getHostName();
            final String parentDir = new File(destPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
            CommandOutput commandOutput = executeCreateDirectoryCommand(deployJvmName, hostName, parentDir);

            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("created the parent dir {} successfully", parentDir);
            } else {
                final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("error in creating parent directory {} :: ERROR : ", parentDir, parentDir);
                throw new DeployApplicationConfException(standardError);
            }
            commandOutput = executeCheckIfFileExistsCommand(deployJvmName, hostName, destPath);

            if (commandOutput.getReturnCode().wasSuccessful()) {
                commandOutput = executeBackUpCommand(deployJvmName, hostName, destPath);

                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    final String standardError = "Failed to back up file " + destPath + " for " + app.getName() + ". Continuing with secure copy.";
                    LOGGER.error(standardError);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
                }
            }
            final CommandOutput execData = executeSecureCopyCommand(deployJvmName, hostName, srcPath, destPath);

            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of {} successful: {}", deployFileName, confFile.getAbsolutePath());
                return execData;
            } else {
                String standardError = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                LOGGER.error("Copy command completed with error trying to copy {} to {} :: ERROR: {}",
                        deployFileName, appName, standardError);
                throw new DeployApplicationConfException(standardError);
            }
        } catch (FileNotFoundException | CommandFailureException ex) {
            LOGGER.error("Failed to deploy config file {} for app {} to jvm {} ", resourceTemplateName, appName, jvmName, ex);
            throw new DeployApplicationConfException(ex);
        } catch (JsonMappingException | JsonParseException e) {
            LOGGER.error("Failed to map meta data while deploying config file {} for app {} to jvm {}", resourceTemplateName, appName, jvmName, e);
            throw new DeployApplicationConfException(e);
        } catch (ResourceFileGeneratorException e){
            LOGGER.error("Fail to generate the resource file {}", resourceTemplateName, e);
            throw new DeployApplicationConfException(e);
        } catch (IOException e) {
            LOGGER.error("Failed for IOException while deploying config file {} for app {} to jvm {}", resourceTemplateName, appName, jvmName, e);
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
        if (theJvms != null && theJvms.size() > 0) {
            Set<String> hostNames = new HashSet<>();
            for (Jvm jvm : theJvms) {
                final String host = jvm.getHostName().toLowerCase();
                if (!hostNames.contains(host)) {
                    hostNames.add(host);
                }
            }
            copyAndExecuteCommand(application, hostNames);
        }
    }

    @Override
    @Transactional
    public void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup){
        List<String> appResourcesNames = groupService.getGroupAppsResourceTemplateNames(groupName);
        Group group = groupService.getGroup(app.getGroup().getId());
        final Set<Jvm> jvms = group.getJvms();
        if (null != appResourcesNames && appResourcesNames.size() > 0) {
            for (String resourceTemplateName : appResourcesNames) {
                String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
                try {
                    ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(resourceTemplateName, app, metaDataStr);
                    if (jvms != null && jvms.size() > 0 && !metaData.getEntity().getDeployToJvms()) {
                        // still need to iterate through the JVMs to get the host names
                        Set<String> hostNames = new HashSet<>();
                        for (Jvm jvm : jvms) {
                            final String host = jvm.getHostName().toLowerCase();
                            if (!hostNames.contains(host)) {
                                hostNames.add(host);
                                groupService.deployGroupAppTemplate(groupName, resourceTemplateName, resourceGroup, app, jvm);
                            }
                        }

                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to map meta data for template {} in group {}", resourceTemplateName, groupName, e);
                    throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to read meta data for template " + resourceTemplateName + " in group " + groupName, e);
                }
            }
        }

    }

    @Override
    @Transactional
    public void copyApplicationWarToHost(Application application, String hostName) {
        if(hostName != null && !hostName.isEmpty()) {
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
            for(String hostName: hostNames) {
                Future<CommandOutput> commandOutputFuture = executeCopyCommand(application, tempWarFile, destPath, null, hostName);
                futures.put(hostName, commandOutputFuture);
            }
            for(Entry<String, Future<CommandOutput>> entry:futures.entrySet()) {
                CommandOutput execData = entry.getValue().get();
                if (execData.getReturnCode().wasSuccessful()) {
                    LOGGER.info("Copy of application war {} to {} was successful", applicationWar.getName(), entry.getKey());
                } else {
                    String errorOutput = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                    LOGGER.error("Copy of application war {} to {} FAILED::{}", applicationWar.getName(), entry.getKey(), errorOutput);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy application war to the group host " + entry.getKey());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Creation of temporary war file for {} FAILED :: {}", application.getWarPath(), e);
            throw new InternalErrorException(AemFaultType.INVALID_PATH, "Failed to create temporary war file for copying to remote hosts");
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("FAILURE getting return status from copying web app war", e);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Exception thrown while copying war", e);
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
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
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
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
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
                            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
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
        return  remoteCommandExecutor.executeRemoteCommand(
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
        return  remoteCommandExecutor.executeRemoteCommand(
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
    @Transactional
    public void copyApplicationConfigToGroupJvms(Group group, final String appName, final ResourceGroup resourceGroup, final User user) {
        final String groupName = group.getName();
        Set<Future> futures = new HashSet<>();

        for (final Jvm jvm : group.getJvms()) {
            final List<String> resourceTemplateNames = applicationPersistenceService.getResourceTemplateNames(appName,
                    jvm.getJvmName());
            for (final String templateName : resourceTemplateNames) {

                final String jvmName = jvm.getJvmName();
                LOGGER.info("Deploying application config {} to JVM {}", templateName, jvmName);

                Future<CommandOutput> commandOutputFuture = executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        return deployConf(appName, groupName, jvmName, templateName, resourceGroup, user);
                    }
                });
                futures.add(commandOutputFuture);
            }
        }
        waitForDeployToComplete(futures);
    }

    protected void waitForDeployToComplete(Set<Future> futures) {
        final int size = futures.size();
        if (size > 0) {
            LOGGER.info("Check to see if all {} tasks completed", size);
            boolean allDone = false;
            // think about adding a manual timeout - for now, since the transaction was timing out before this was added fall back to the transaction timeout
            while (!allDone) {
                boolean isDone = true;
                for (Future isDoneFuture : futures) {
                    isDone = isDone && isDoneFuture.isDone();
                }
                allDone = isDone;
            }
            LOGGER.info("Tasks complete: {}", size);
        }
    }

    /**
     * As the name describes, this method creates the path if it does not exists.
     */
    protected static void createPathIfItDoesNotExists(String path) {
        if (!Files.exists(Paths.get(path))) {
            new File(path).mkdir();
        }
    }

    /**
     * Create application configuration file.
     *
     * @param appName              - the application name.
     * @param groupName            - the group where the application belongs to.
     * @param jvmName              - the JVM name where the application is deployed.
     * @param resourceTemplateName - the name of the resource to generate.
     * @return the configuration file.
     */
    @Transactional
    protected File createConfFile(final String appName, final String groupName, final String jvmName,
                                  final String resourceTemplateName, final ResourceGroup resourceGroup)
            throws FileNotFoundException {
        PrintWriter out = null;
        final StringBuilder fileNameBuilder = new StringBuilder();

        createPathIfItDoesNotExists(ApplicationProperties.get(GENERATED_RESOURCE_DIR));
        createPathIfItDoesNotExists(ApplicationProperties.get(GENERATED_RESOURCE_DIR) + "/"
                + groupName.replace(" ", "-"));
        createPathIfItDoesNotExists(ApplicationProperties.get(GENERATED_RESOURCE_DIR)
                + "/" + groupName.replace(" ", "-") + "/" + jvmName.replace(" ", "-"));

        fileNameBuilder.append(ApplicationProperties.get(GENERATED_RESOURCE_DIR))
                .append('/')
                .append(groupName.replace(" ", "-"))
                .append('/')
                .append(jvmName.replace(" ", "-"))
                .append('/')
                .append(appName.replace(" ", "-"))
                .append('.')
                .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()))
                .append('_')
                .append(resourceTemplateName);

        final File appConfFile = new File(fileNameBuilder.toString());
        try {
            out = new PrintWriter(appConfFile.getAbsolutePath());
            out.println(getResourceTemplate(appName, groupName, jvmName, resourceTemplateName, resourceGroup, true));
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return appConfFile;
    }

    @Override
    @Transactional
    public Application uploadWebArchive(final Identifier<Application> appId, final String warName, final byte[] war,
                                        final String deployPath) throws IOException {

        final Map<String, Object> metaDataMap = new HashMap<>();

        if (warName != null && warName.toLowerCase().endsWith(".war")) {

            final Application application = applicationPersistenceService.getApplication(appId);

            metaDataMap.put("contentType", ContentType.APPLICATION_BINARY.contentTypeStr);
            metaDataMap.put("deployPath", StringUtils.isEmpty(deployPath) ? ApplicationProperties.get(JWALA_WEBAPPS_DIR)
                                                                          : deployPath);
            metaDataMap.put("deployFileName", warName);
            metaDataMap.put("templateName", warName);

            final Entity entity = new Entity();
            entity.setGroup(application.getGroup().getName());
            entity.setDeployToJvms(false);
            metaDataMap.put("unpack", application.isUnpackWar());
            metaDataMap.put("overwrite", false);

            // Note: This is for backward compatibility.
            entity.setTarget(application.getName());
            entity.setType(EntityType.GROUPED_APPS.toString());

            metaDataMap.put("entity", entity);

            final ResourceTemplateMetaData metaData =
                    ResourceTemplateMetaData.createFromJsonStr(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(metaDataMap));

            InputStream resourceDataIn = new ByteArrayInputStream(war);
            resourceDataIn = new ByteArrayInputStream(resourceService.uploadResource(metaData, resourceDataIn).getBytes());

            // Creating a group level app resource is not straightforward, there are business logic involved
            // that only the resources service knows of so we just reuse it.
            final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setResourceName(metaData.getTemplateName()).setGroupName(application.getGroup().getName())
                    .setWebAppName(application.getName()).build();
            resourceService.createResource(resourceIdentifier, metaData, resourceDataIn);

            application.setWarName(warName);
            application.setWarPath(resourceService.getAppTemplate(application.getGroup().getName(), application.getName(), warName));

            return application;
        } else {
            throw new ApplicationServiceException("Invalid war file!");
        }
    }

    @Override
    public void deployConf(final String appName, final String hostName, final User user) {
        final Application application = applicationPersistenceService.getApplication(appName);
        final Group group = groupService.getGroup(application.getGroup().getId());
        final String groupName = group.getName();
        final Set<String> resourceSet = new HashSet<>();
        final List<String> hostNames = new ArrayList<>();
        final List<String> allHosts = groupService.getAllHosts();
        if (allHosts == null || allHosts.isEmpty()) {
            LOGGER.error("No hosts found for the group: {} and application: {}", groupName, appName);
            throw new InternalErrorException(AemFaultType.GROUP_MISSING_HOSTS, "No host found for the application " + appName);
        }
        if (hostName == null || hostName.isEmpty()) {
            LOGGER.info("Hostname not passed, deploying to all hosts");
            for (String host : allHosts) {
                hostNames.add(host.toLowerCase());
            }
        } else {
            LOGGER.info("host name provided {}", hostName);
            for (final String host : allHosts) {
                if (hostName.toLowerCase().equals(host.toLowerCase())) {
                    hostNames.add(host.toLowerCase());
                }
            }
            if (hostNames == null || hostNames.isEmpty()) {
                LOGGER.error("Hostname {} does not belong to the group {}", hostName, groupName);
                throw new InternalErrorException(AemFaultType.INVALID_HOST_NAME, "The hostname: " + hostName + " does not belong to the group " + groupName);
            }
        }
        LOGGER.debug("deploying templates to hosts: {}", hostNames.toString());
        Set<String> errorJvms = new HashSet<>();
        for (Jvm jvm : group.getJvms()) {
            if (hostNames.contains(jvm.getHostName().toLowerCase()) && jvm.getState().isStartedState()) {
                errorJvms.add(jvm.getJvmName());
            }
        }
        if (!errorJvms.isEmpty()) {
            LOGGER.error("Jvms {} not stopped, make sure the jvms are stopped before deploying", errorJvms.toString());
            throw new InternalErrorException(AemFaultType.RESOURCE_DEPLOY_FAILURE,
                    "Make sure the following Jvms " + errorJvms.toString() + " are completely stopped before deploying.");
        }
        List<String> resourceTemplates = groupService.getGroupAppsResourceTemplateNames(groupName);
        for (String resourceTemplate : resourceTemplates) {
            String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplate);
            LOGGER.debug("metadata for template: {} is {}", resourceTemplate, metaDataStr);
            try {
                ResourceTemplateMetaData metaData = resourceService.getMetaData(metaDataStr);
                if (!metaData.getEntity().getDeployToJvms() && !resourceSet.contains(resourceTemplate)) {
                    LOGGER.info("Template {} needs to be deployed adding it to the list", resourceTemplate);
                    resourceSet.add(resourceTemplate);
                }
            } catch (IOException e) {
                LOGGER.error("Error in templatizing the metadata file", e);
                throw new InternalErrorException(AemFaultType.IO_EXCEPTION, "Error in templatizing the metadata for resource " + resourceTemplate);
            }
        }
        final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Future<Map<String, CommandOutput>>> futures = new HashMap<>();
        for (final String host : hostNames) {
            final String key = application.getName() + "/" + host;
            if (!writeLock.containsKey(key)) {
                writeLock.put(key, new ReentrantReadWriteLock());
            }
                if (writeLock.get(key).isWriteLocked()) {
                    throw new InternalErrorException(AemFaultType.SERVICE_EXCEPTION, "Current resource is being deployed, wait for deploy to complete.");
                }
                Future<Map<String, CommandOutput>> commandOutputFutureMap = executorService.submit(new Callable<Map<String, CommandOutput>>() {
                    @Override
                    public Map<String, CommandOutput> call() throws Exception {
                        writeLock.get(key).writeLock().lock();
                        try {
                            Map<String, CommandOutput> commandOutputs = new HashMap<>();
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            for (final String resource : resourceSet) {
                                LOGGER.info("Deploying {} to host {}", resource, host);
                                commandOutputs.put(resource, groupService.deployGroupAppTemplate(groupName, resource, resourceGroup, application, host));
                            }
                            return commandOutputs;
                        } finally {
                            writeLock.get(key).writeLock().unlock();
                        }
                    }
                }
                );
                futures.put(key, commandOutputFutureMap);
        }

        for (Entry<String, Future<Map<String, CommandOutput>>> entry : futures.entrySet()) {
            try {
                String[] resourceData = entry.getKey().split("/");
                Map<String, CommandOutput> commandOutputMap = entry.getValue().get();
                for (Entry<String, CommandOutput> commandOutput : commandOutputMap.entrySet()) {
                    if (!commandOutput.getValue().getReturnCode().wasSuccessful()) {
                        final String errorMessage = "Error in deploying resource " + commandOutput.getKey() +
                                " to host " + resourceData[1] + " for application " + resourceData[0];
                        LOGGER.error(errorMessage);
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, errorMessage);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Error in executing deploy", e);
                throw new InternalErrorException(AemFaultType.RESOURCE_DEPLOY_FAILURE, e.getMessage());
            }
        }
    }
}
