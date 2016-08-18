package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
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
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.template.ResourceFileGenerator;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String STP_WEBAPPS_DIR = "stp.webapps.dir";
    private final ExecutorService executorService;

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

    private GroupService groupService;
    private final HistoryService historyService;
    private final MessagingService messagingService;

    public ApplicationServiceImpl(final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final RemoteCommandExecutor<ApplicationControlOperation> applicationCommandService,
                                  final GroupService groupService,
                                  final WebArchiveManager webArchiveManager,
                                  final PrivateApplicationService privateApplicationService,
                                  final HistoryService historyService,
                                  final MessagingService messagingService,
                                  final ResourceService resourceService) {
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationCommandExecutor = applicationCommandService;
        this.groupService = groupService;
        this.webArchiveManager = webArchiveManager;
        this.privateApplicationService = privateApplicationService;
        this.historyService = historyService;
        this.messagingService = messagingService;
        this.resourceService = resourceService;
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
            return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, application);
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
            ObjectMapper mapper = new ObjectMapper();
            ResourceTemplateMetaData templateMetaData = mapper.readValue(metaData, ResourceTemplateMetaData.class);
            String metaDataPath = templateMetaData.getDeployPath();

            app.setParentJvm(jvm);
            final String destPath = ResourceFileGenerator.generateResourceConfig(metaDataPath, resourceGroup, app) + '/' + resourceTemplateName;
            String srcPath;
            if (templateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
                srcPath = applicationPersistenceService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
            } else {
                srcPath = confFile.getAbsolutePath().replace("\\", "/");
            }

            final String eventDescription = WindowsJvmNetOperation.SECURE_COPY.name() + " " + resourceTemplateName;
            final String id = user.getId();

            historyService.createHistory("JVM " + jvm.getJvmName(), new ArrayList<Group>(jvm.getGroups()), eventDescription, EventType.USER_ACTION, id);
            messagingService.send(new JvmHistoryEvent(jvm.getId(), eventDescription, id, DateTime.now(), JvmControlOperation.SECURE_COPY));

            final String deployJvmName = jvm.getJvmName();
            final String hostName = jvm.getHostName();
            CommandOutput commandOutput = applicationCommandExecutor.executeRemoteCommand(
                    deployJvmName,
                    hostName,
                    ApplicationControlOperation.CHECK_FILE_EXISTS,
                    new WindowsApplicationPlatformCommandProvider(),
                    destPath);
            if (commandOutput.getReturnCode().wasSuccessful()) {
                String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
                final String destPathBackup = destPath + currentDateSuffix;
                commandOutput = applicationCommandExecutor.executeRemoteCommand(
                        deployJvmName,
                        hostName,
                        ApplicationControlOperation.BACK_UP_FILE,
                        new WindowsApplicationPlatformCommandProvider(),
                        destPath,
                        destPathBackup);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    LOGGER.error("Failed to back up file {} for {}. Continuing with secure copy.", destPath, app.getName());
                }
            }
            final CommandOutput execData = applicationCommandExecutor.executeRemoteCommand(
                    deployJvmName,
                    hostName,
                    ApplicationControlOperation.SECURE_COPY,
                    new WindowsApplicationPlatformCommandProvider(),
                    srcPath,
                    destPath);
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of {} successful: {}", resourceTemplateName, confFile.getAbsolutePath());
                return execData;
            } else {
                String standardError = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                LOGGER.error("Copy command completed with error trying to copy {} to {} :: ERROR: {}",
                        resourceTemplateName, appName, standardError);
                throw new DeployApplicationConfException(standardError);
            }
        } catch (FileNotFoundException | CommandFailureException ex) {
            LOGGER.error("Failed to deploy config file {} for app {} to jvm {} ", resourceTemplateName, appName, jvmName, ex);
            throw new DeployApplicationConfException(ex);
        } catch (JsonMappingException | JsonParseException e) {
            LOGGER.error("Failed to map meta data while deploying config file {} for app {} to jvm {}", resourceTemplateName, appName, jvmName, e);
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
    public String previewResourceTemplate(String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup) {
        final Application application;
        if (StringUtils.isNotEmpty(jvmName)) {
            application = applicationPersistenceService.findApplication(appName, groupName, jvmName);
            application.setParentJvm(jvmPersistenceService.findJvmByExactName(jvmName));
        } else {
            application = applicationPersistenceService.getApplication(appName);
        }
        return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, application);
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
                    ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
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
            final String destPath = ApplicationProperties.get("stp.webapps.dir");
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
                LOGGER.info("Copying {} war to host {}", name, host);
                CommandOutput commandOutput = applicationCommandExecutor.executeRemoteCommand(null, host, ApplicationControlOperation.SECURE_COPY, new WindowsApplicationPlatformCommandProvider(), tempWarFile.getAbsolutePath().replaceAll("\\\\", "/"), destPath);

                if (application.isUnpackWar()) {
                    final String warName = application.getWarName();
                    LOGGER.info("Unpacking war {} on host {}", warName, host);

                    // create the .toc directory as the destination for the unpack-war script
                    final String tocScriptsPath = AemControl.Properties.USER_TOC_SCRIPTS_PATH.getValue();
                    commandOutput = applicationCommandExecutor.executeRemoteCommand(null, host, ApplicationControlOperation.CREATE_DIRECTORY, new WindowsApplicationPlatformCommandProvider(), tocScriptsPath);
                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        return commandOutput; // return immediately if creating the dir failed
                    }

                    // copy the unpack war script to .toc
                    final String unpackWarScriptPath = ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.UNPACK_WAR_SCRIPT_NAME;
                    commandOutput = applicationCommandExecutor.executeRemoteCommand(null, host, ApplicationControlOperation.SECURE_COPY, new WindowsApplicationPlatformCommandProvider(), unpackWarScriptPath, tocScriptsPath);
                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        return commandOutput; // return immediately if the copy failed
                    }

                    // make sure the scripts are executable
                    commandOutput = applicationCommandExecutor.executeRemoteCommand(null, host, ApplicationControlOperation.CHANGE_FILE_MODE, new WindowsApplicationPlatformCommandProvider(), "a+x", tocScriptsPath, "*.sh");
                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        return commandOutput;
                    }

                    // call the unpack war script
                    commandOutput = applicationCommandExecutor.executeRemoteCommand(null, host, ApplicationControlOperation.UNPACK_WAR, new WindowsApplicationPlatformCommandProvider(), warName);
                }
                return commandOutput;
            }
        });
        return commandOutputFuture;
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
        if (warName != null && warName.toLowerCase().endsWith(".war")) {

            final Application application = applicationPersistenceService.getApplication(appId);
            final ResourceTemplateMetaData metaData = new ResourceTemplateMetaData();

            metaData.setContentType(ContentType.APPLICATION_BINARY.contentTypeStr);
            metaData.setDeployPath(StringUtils.isEmpty(deployPath) ?
                                                   ApplicationProperties.get(STP_WEBAPPS_DIR) : deployPath);
            metaData.setDeployFileName(warName);
            metaData.setTemplateName(warName);

            final Entity entity = new Entity();
            entity.setGroup(application.getGroup().getName());
            entity.setDeployToJvms(false);
            metaData.setUnpack(application.isUnpackWar());

            // Note: This is for backward compatibility.
            entity.setTarget(application.getName());
            entity.setType(EntityType.GROUPED_APPS.toString());

            metaData.setEntity(entity);

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
}
