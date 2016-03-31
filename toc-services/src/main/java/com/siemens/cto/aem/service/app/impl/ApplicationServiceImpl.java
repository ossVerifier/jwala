package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.app.*;
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
import com.siemens.cto.aem.template.GeneratorUtils;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.apache.commons.lang.StringUtils;
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
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String APP_CONF_PATH = "paths.app.conf";
    private static final String APP_CONF_PATH_INSECURE = "paths.app.conf.insecure";
    private static final String JVM_INSTANCE_DIR = "paths.instances";
    private static final String GENERATED_RESOURCE_DIR = "stp.generated.resource.dir";
    private static final String APP_CONTEXT_TEMPLATE = "stp.app.context.template";
    private static final String ROLE_MAPPING_PROPERTIES_TEMPLATE = "stp.app.role.mapping.properties.template";
    private static final String APP_PROPERTIES_TEMPLATE = "stp.app.properties.template";
    private static final String STR_PROPERTIES = "properties";
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

        final String appContext = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_CONTEXT_TEMPLATE));
        final String roleMappingProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(ROLE_MAPPING_PROPERTIES_TEMPLATE));
        final String appProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_PROPERTIES_TEMPLATE));

        final Application application = applicationPersistenceService.createApplication(createApplicationRequest, appContext, roleMappingProperties, appProperties);

        groupService.populateGroupAppTemplates(application, appContext, roleMappingProperties, appProperties);

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
                                      final boolean tokensReplaced) {
        final String template = applicationPersistenceService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
        if (tokensReplaced) {
            final Map<String, Application> bindings = new HashMap<>();
            Jvm jvm = jvmPersistenceService.findJvm(jvmName, groupName);
            bindings.put("webApp", new WebApp(applicationPersistenceService.findApplication(appName, groupName, jvmName), jvm));
            try {
                return GeneratorUtils.bindDataToTemplateText(bindings, template);
            } catch (Exception x) {
                throw new ApplicationException("Template token replacement failed.", x);
            }
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
                                    final String resourceTemplateName, boolean backUp, User user) {

        final StringBuilder key = new StringBuilder();
        key.append(groupName).append(jvmName).append(appName).append(resourceTemplateName);

        try {
            // only one at a time per web server
            if (!writeLock.containsKey(key.toString())) {
                writeLock.put(key.toString(), new ReentrantReadWriteLock());
            }
            writeLock.get(key.toString()).writeLock().lock();

            final File confFile = createConfFile(appName, groupName, jvmName, resourceTemplateName);
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM must be stopped before attempting to update the resource files");
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }
            final Application app = applicationPersistenceService.findApplication(appName, groupName, jvmName);

            final StringBuilder target = new StringBuilder();
            if (getFileExtension(resourceTemplateName).equalsIgnoreCase(STR_PROPERTIES)) {
                target.append(System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH).replace("\\", "/")).append('/');
            } else {
                String appConfPath;
                if (app.isSecure()) {
                    appConfPath = ApplicationProperties.get(APP_CONF_PATH);
                } else {
                    appConfPath = ApplicationProperties.get(APP_CONF_PATH_INSECURE);
                    if (StringUtils.isEmpty(appConfPath)) {
                        // fall back on secure
                        appConfPath = ApplicationProperties.get(APP_CONF_PATH);
                    }
                }

                target.append(ApplicationProperties.get(JVM_INSTANCE_DIR))
                        .append('/')
                        .append(jvmName)
                        .append('/')
                        .append(appConfPath)
                        .append('/');
            }

            target.append(resourceTemplateName);

            final String destPath = target.toString();
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
                    ApplicationControlOperation.DEPLOY_CONFIG_FILE,
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
            throw new DeployApplicationConfException(ex);
        } finally {
            writeLock.get(key.toString()).writeLock().unlock();
        }
    }

    @Override
    @Transactional
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, User user) {
        uploadAppTemplateRequest.validate();
        Jvm appJvm = null;
        // if the template is the context xml for the app then associate it with a jvm
        if (uploadAppTemplateRequest.getConfFileName().endsWith(".xml")) {
            final String jvmName = uploadAppTemplateRequest.getJvmName();
            for (Jvm jvm : jvmPersistenceService.findJvms(jvmName)) {
                if (jvm.getJvmName().equals(jvmName)) {
                    appJvm = jvm;
                    break;
                }
            }
        }
        JpaJvm jpaJvm = appJvm != null ? jvmPersistenceService.getJpaJvm(appJvm.getId(), false) : null;
        return applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String appName, String groupName, String jvmName, String template) {
        final Map<String, Application> bindings = new HashMap<>();
        bindings.put("webApp", new WebApp(applicationPersistenceService.findApplication(appName, groupName, jvmName),
                jvmPersistenceService.findJvm(jvmName, groupName)));
        return GeneratorUtils.bindDataToTemplateText(bindings, template);
    }

    @Override
    @Transactional
    public void copyApplicationWarToGroupHosts(Application application) {
        File applicationWar = new File(application.getWarPath());
        final String sourcePath = applicationWar.getParent();
        File tempWarFile = new File(sourcePath + "/" + application.getWarName());
        Map<String, Future<CommandOutput>> futures = new HashMap<>();
        try {
            FileCopyUtils.copy(applicationWar, tempWarFile);
            final String destPath = ApplicationProperties.get("stp.webapps.dir");
            Group group = groupService.getGroup(application.getGroup().getId());
            final Set<Jvm> theJvms = group.getJvms();
            if (theJvms != null && theJvms.size() > 0) {
                Set<String> hostNames = new HashSet<>();
                for (Jvm jvm : theJvms) {
                    final String host = jvm.getHostName().toLowerCase();
                    if (hostNames.contains(host)) {
                        continue;
                    } else {
                        hostNames.add(host);
                    }
                    Future<CommandOutput> commandOutputFuture = executeCopyCommand(application, tempWarFile, destPath, jvm, host);
                    futures.put(host, commandOutputFuture);
                }
                for (String keyHostName : futures.keySet()) {
                    CommandOutput execData = futures.get(keyHostName).get();
                    if (execData.getReturnCode().wasSuccessful()) {
                        LOGGER.info("Copy of application war {} to {} was successful", applicationWar.getName(), keyHostName);
                    } else {
                        String errorOutput = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                        LOGGER.error("Copy of application war {} to {} FAILED::{}", applicationWar.getName(), keyHostName, errorOutput);
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy application war to the group host " + keyHostName);
                    }
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

    protected Future<CommandOutput> executeCopyCommand(final Application application, final File tempWarFile, final String destPath, final Jvm jvm, final String host){
        final String name = application.getName();
        Future<CommandOutput> commandOutputFuture = executorService.submit(new Callable<CommandOutput>() {
            @Override
            public CommandOutput call() throws Exception {
                LOGGER.info("Copying {} war to host {}", name, host);
                final String jvmName = jvm.getJvmName();
                final String hostName = jvm.getHostName();
                CommandOutput commandOutput = applicationCommandExecutor.executeRemoteCommand(jvmName, hostName, ApplicationControlOperation.DEPLOY_WAR, new WindowsApplicationPlatformCommandProvider(), tempWarFile.getAbsolutePath().replaceAll("\\\\", "/"), destPath);
                if (application.isUnpackWar()) {
                    final String warName = application.getWarName();
                    LOGGER.info("Unpacking war {} on host {}", warName, hostName);
                    commandOutput = applicationCommandExecutor.executeRemoteCommand(jvmName, hostName, ApplicationControlOperation.UNPACK_WAR, new WindowsApplicationPlatformCommandProvider(), warName);
                }
                return commandOutput;
            }
        });
        return commandOutputFuture;
    }

    @Override
    @Transactional
    public void copyApplicationConfigToGroupJvms(Group group, final String appName, final User user) {
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
                        return deployConf(appName, groupName, jvmName, templateName, doNotBackUpOriginals, user);
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
    public void deployConfToOtherJvmHosts(String appName, String groupName, String jvmName, String resourceTemplateName, User user) {
        List<String> deployedHosts = new ArrayList<>();
        deployedHosts.add(jvmPersistenceService.findJvm(jvmName, groupName).getHostName());
        Set<Jvm> jvmSet = groupService.getGroup(groupName).getJvms();
        if (!jvmSet.isEmpty()) {
            for (Jvm jvm : jvmSet) {
                final String hostName = jvm.getHostName();
                if (!deployedHosts.contains(hostName)) {
                    final boolean doBackUpOriginal = true;
                    deployConf(appName, groupName, jvm.getJvmName(), resourceTemplateName, doBackUpOriginal, user);
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
                                  final String resourceTemplateName)
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
            out.println(getResourceTemplate(appName, groupName, jvmName, resourceTemplateName, true));
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return appConfFile;
    }

    @Override
    @Transactional
    public void createAppConfigTemplateForJvm(Jvm jvm, Application app, Identifier<Group> groupId) {
        applicationPersistenceService.createApplicationConfigTemplateForJvm(jvm.getJvmName(), app, groupId, fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_CONTEXT_TEMPLATE)));
    }

    /**
     * Inner class application wrapper to include a web application's parent JVM.
     */
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
