package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.app.*;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String GENERATED_RESOURCE_DIR = "stp.generated.resource.dir";
    private static final String APP_CONTEXT_TEMPLATE = "stp.app.context.template";
    private static final String PATHS_RESOURCE_TYPES = "paths.resource-types";
    private final ExecutorService executorService;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private WebArchiveManager webArchiveManager;

    @Autowired
    private PrivateApplicationService privateApplicationService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    private RemoteCommandExecutor<ApplicationControlOperation> applicationCommandExecutor;

    private Map<String, ReentrantReadWriteLock> writeLock = new HashMap<>();

    private GroupService groupService;

    @Autowired
    private final FileManager fileManager;

    public ApplicationServiceImpl(final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final RemoteCommandExecutor<ApplicationControlOperation> applicationCommandService,
                                  final GroupService groupService,
                                  final FileManager fileManager,
                                  final WebArchiveManager webArchiveManager,
                                  final PrivateApplicationService privateApplicationService) {
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationCommandExecutor = applicationCommandService;
        this.groupService = groupService;
        this.fileManager = fileManager;
        this.webArchiveManager = webArchiveManager;
        this.privateApplicationService = privateApplicationService;
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

//      TODO do not propagate the default application templates since they are healthcheck specific
//        final String appContext = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_CONTEXT_TEMPLATE));
//        final String roleMappingProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(ROLE_MAPPING_PROPERTIES_TEMPLATE));
//        final String appProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_PROPERTIES_TEMPLATE));

        String appContext = "";
        String roleMappingProperties = "";
        String appProperties = "";
        final Application application = applicationPersistenceService.createApplication(createApplicationRequest, appContext, roleMappingProperties, appProperties);

        // TODO do not propagate the default application templates since they are healthcheck specific
        // groupService.populateGroupAppTemplates(application, appContext, roleMappingProperties, appProperties);

        return application;
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

    @Transactional
    @Override
    public Application deleteWebArchive(Identifier<Application> appId, User user) {

        Application app = this.getApplication(appId);
        RemoveWebArchiveRequest rwac = new RemoveWebArchiveRequest(app);

        RepositoryFileInformation result = RepositoryFileInformation.none();

        try {
            result = webArchiveManager.remove(rwac);
            LOGGER.info("Archive Delete: " + result.toString());
        } catch (IOException e) {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Error deleting archive.", e);
        }

        if (result.getType() == Type.DELETED) {
            return applicationPersistenceService.removeWarPath(rwac);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Archive not found to delete.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String appName) {
        return applicationPersistenceService.getResourceTemplateNames(appName);
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

    /**
     * Returns the extension of the filename.
     * e.g. roleMapping.properties will return "properties".
     *
     * @param fileName
     * @return extension of the filename.
     */
    protected static String getFileExtension(final String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    @Transactional
    // TODO: Have an option to do a hot deploy or not.
    public CommandOutput deployConf(final String appName, final String groupName, final String jvmName,
                                    final String resourceTemplateName, boolean backUp, ResourceGroup resourceGroup, User user) {

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
            String metaDataPath = templateMetaData.getPath();

            app.setParentJvm(jvm);
            final String destPath = ResourceFileGenerator.generateResourceConfig(metaDataPath, resourceGroup, app) + '/' + resourceTemplateName;
            final String srcPath = confFile.getAbsolutePath().replace("\\", "/");
            // back up the original file first only if the war was uploaded
            // if the war wasn't uploaded yet then there is no file to backup
            final String deployJvmName = jvm.getJvmName();
            final String hostName = jvm.getHostName();
            if (app.getWarPath() != null && backUp) {
                String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
                final String destPathBackup = destPath + currentDateSuffix;
                final CommandOutput commandOutput = applicationCommandExecutor.executeRemoteCommand(
                        deployJvmName,
                        hostName,
                        ApplicationControlOperation.BACK_UP_CONFIG_FILE,
                        new WindowsApplicationPlatformCommandProvider(),
                        destPath,
                        destPathBackup);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + jvm);
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
        final Application application = applicationPersistenceService.findApplication(appName, groupName, jvmName);
        application.setParentJvm(jvmPersistenceService.findJvmByExactName(jvmName));
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
        final List<String> resourceTemplateNames = applicationPersistenceService.getResourceTemplateNames(appName);
        Set<Future> futures = new HashSet<>();

        for (final Jvm jvm : group.getJvms()) {
            for (final String templateName : resourceTemplateNames) {

                final String jvmName = jvm.getJvmName();
                LOGGER.info("Deploying application config {} to JVM {}", templateName, jvmName);
                final boolean doNotBackUpOriginals = false;

                Future<CommandOutput> commandOutputFuture = executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        return deployConf(appName, groupName, jvmName, templateName, doNotBackUpOriginals, resourceGroup, user);
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

    @Override
    @Transactional
    public void deployConfToOtherJvmHosts(String appName, String groupName, String jvmName, String resourceTemplateName, ResourceGroup resourceGroup, User user) {
        List<String> deployedHosts = new ArrayList<>();
        deployedHosts.add(jvmPersistenceService.findJvm(jvmName, groupName).getHostName());
        Set<Jvm> jvmSet = groupService.getGroup(groupName).getJvms();
        if (!jvmSet.isEmpty()) {
            for (Jvm jvm : jvmSet) {
                final String hostName = jvm.getHostName();
                if (!deployedHosts.contains(hostName)) {
                    final boolean doBackUpOriginal = true;
                    deployConf(appName, groupName, jvm.getJvmName(), resourceTemplateName, doBackUpOriginal, resourceGroup, user);
                    deployedHosts.add(jvm.getHostName());
                }
            }
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
    @Deprecated
    // TODO: Replace with the generic resource template approach (stop using fileManager also).
    public void createAppConfigTemplateForJvm(Jvm jvm, Application app, Identifier<Group> groupId) {
        String metaData;
        try {
            metaData = FileUtils.readFileToString(new File(ApplicationProperties.get(PATHS_RESOURCE_TYPES) + "/" +
                    ApplicationProperties.get(APP_CONTEXT_TEMPLATE) + ".json"));
        } catch (IOException ioe) {
            metaData = ""; // Note: This is bad code! This is just an interim solution since this method will be refactored
                           // not to use fileManager in addition Fileutils can't easily be mocked hence the reason for not
                           //  throwing an exception here!
            LOGGER.error("Failed to get meta data for JVM {} template", jvm.getJvmName(), ioe);
        }

        applicationPersistenceService.createApplicationConfigTemplateForJvm(jvm.getJvmName(), app, groupId,
                metaData, fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_CONTEXT_TEMPLATE)));
    }

    /**
     * Inner class application wrapper to include a web application's parent JVM.
     */
    @Deprecated
    private class WebApp extends Application {
        final Jvm jvm;

        public WebApp(final Application app, final Jvm parentJvm) {
            super(app.getId(), app.getName(), app.getWarPath(), app.getWebAppContext(), app.getGroup(), app.isSecure(),
                    app.isLoadBalanceAcrossServers(), app.isUnpackWar(), app.getWarName());
            jvm = parentJvm;
        }

        @SuppressWarnings("unused")
            // used by template bindings
        Jvm getJvm() {
            return jvm;
        }
    }
}
