package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.domain.command.app.*;
import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public class ApplicationServiceImpl implements ApplicationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String APP_CONF_PATH = "paths.app.conf";
    private static final String APP_CONF_PATH_INSECURE = "paths.app.conf.insecure";
    private static final String JVM_INSTANCE_DIR = "paths.instances";
    private static final String GENERATED_RESOURCE_DIR = "stp.generated.resource.dir";
    private static final String APP_CONTEXT_TEMPLATE = "stp.app.context.template";
    private static final String ROLE_MAPPING_PROPERTIES_TEMPLATE = "stp.app.role.mapping.properties.template";
    private static final String APP_PROPERTIES_TEMPLATE = "stp.app.properties.template";
    private static final String STR_PROPERTIES = "properties";

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private WebArchiveManager webArchiveManager;

    @Autowired
    private PrivateApplicationService privateApplicationService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    @Autowired
    private ApplicationCommandService applicationCommandService;

    private Map<String, ReentrantReadWriteLock> writeLock = new HashMap<>();

    @Autowired
    private JvmDao jvmDao;

    private AemSshConfig aemSshConfig;

    private GroupService groupService;

    @Autowired
    private final FileManager fileManager;
    public ApplicationServiceImpl(final ApplicationDao applicationDao,
                                  final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final ClientFactoryHelper clientFactoryHelper,
                                  final ApplicationCommandService applicationCommandService,
                                  final JvmDao jvmDao,
                                  final AemSshConfig aemSshConfig,
                                  final GroupService groupService,
                                  final FileManager fileManager,
                                  final WebArchiveManager webArchiveManager,
                                  final PrivateApplicationService privateApplicationService) {
        this.applicationDao = applicationDao;
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.applicationCommandService = applicationCommandService;
        this.jvmDao = jvmDao;
	    this.aemSshConfig = aemSshConfig;
        this.groupService = groupService;
	    this.fileManager = fileManager;
        this.webArchiveManager = webArchiveManager;
        this.privateApplicationService = privateApplicationService;
    }

    @Transactional(readOnly = true)
    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationDao.getApplication(aApplicationId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> getApplications() {
        return applicationDao.getApplications();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplications(Identifier<Group> groupId) {
        return applicationDao.findApplicationsBelongingTo(groupId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId) {
        return applicationDao.findApplicationsBelongingToJvm(jvmId);
    }

    @Transactional
    @Override
    public Application updateApplication(UpdateApplicationCommand anUpdateCommand, User anUpdatingUser) {
        anUpdateCommand.validateCommand();

        final Event<UpdateApplicationCommand> event = new Event<>(anUpdateCommand,
                AuditEvent.now(anUpdatingUser));

        return applicationPersistenceService.updateApplication(event);
    }

    @Transactional
    @Override
    public Application createApplication(final CreateApplicationCommand aCreateAppCommand,
                                         final User aCreatingUser) {

        aCreateAppCommand.validateCommand();
        final Event<CreateApplicationCommand> event = new Event<>(aCreateAppCommand,
                AuditEvent.now(aCreatingUser));

        final String appContext = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_CONTEXT_TEMPLATE));
        final String roleMappingProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(ROLE_MAPPING_PROPERTIES_TEMPLATE));
        final String appProperties = fileManager.getResourceTypeTemplate(ApplicationProperties.get(APP_PROPERTIES_TEMPLATE));
        return applicationPersistenceService.createApplication(event, appContext, roleMappingProperties, appProperties);
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
    public Application uploadWebArchive(UploadWebArchiveCommand command, User user) {
        command.validateCommand();

        Event<UploadWebArchiveCommand> event = Event.create(command, AuditEvent.now(user));

        return privateApplicationService.uploadWebArchiveUpdateDB(event, privateApplicationService.uploadWebArchiveData(event));
    }

    @Transactional
    @Override
    public Application deleteWebArchive(Identifier<Application> appId, User user) {

        Application app = this.getApplication(appId);
        RemoveWebArchiveCommand rwac = new RemoveWebArchiveCommand(app);
        Event<RemoveWebArchiveCommand> event = Event.create(rwac, AuditEvent.now(user));

        RepositoryFileInformation result = RepositoryFileInformation.none();

        try {
            result = webArchiveManager.remove(event);
            LOGGER.info("Archive Delete: " + result.toString());
        } catch (IOException e) {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Error deleting archive.", e);
        }

        if (result.getType() == Type.DELETED) {
            return applicationPersistenceService.removeWARPath(event);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_WAR, "Archive not found to delete.");
        }
    }

    @Override
    public List<String> getResourceTemplateNames(final String appName) {
        return applicationPersistenceService.getResourceTemplateNames(appName);
    }

    @Override
    public String getResourceTemplate(final String appName,
                                      final String groupName,
                                      final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = applicationPersistenceService.getResourceTemplate(appName, resourceTemplateName);
        if (tokensReplaced) {
            final Map<String, Application> bindings = new HashMap<>();
            bindings.put("webApp", new WebApp(applicationDao.findApplication(appName, groupName, jvmName),
                    jvmDao.findJvm(jvmName, groupName)));
            try {
                return GeneratorUtils.bindDataToTemplateText(bindings, template);
            } catch(Exception x) {
                throw new ApplicationException("Template token replacement failed.",x);
            }
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template) {
        return applicationPersistenceService.updateResourceTemplate(appName, resourceTemplateName, template);
    }

    /**
     * Returns the extension of the filename.
     * e.g. roleMapping.properties will return "properties".
     * @param fileName
     * @return extension of the filename.
     */
    private static String getFileExtension(final String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    @Override
    @Transactional(readOnly = true)
    // TODO: Have an option to do a hot deploy or not.
    public CommandOutput deployConf(final String appName, final String groupName, final String jvmName,
                               final String resourceTemplateName, User user) {

        final StringBuilder key = new StringBuilder();
        key.append(groupName).append(jvmName).append(appName).append(resourceTemplateName);

        try {
            // only one at a time per web server
            if (!writeLock.containsKey(key.toString())) {
                writeLock.put(key.toString(), new ReentrantReadWriteLock());
            }
            writeLock.get(key.toString()).writeLock().lock();

            final File confFile = createConfFile(appName, groupName, jvmName, resourceTemplateName);
            final Jvm jvm = jvmPersistenceService.findJvms(jvmName).get(0);
            final Application app = applicationDao.findApplication(appName, groupName, jvmName);

            final StringBuilder target = new StringBuilder();
            if (getFileExtension(resourceTemplateName).equalsIgnoreCase(STR_PROPERTIES)) {
                target.append(System.getProperty(AemConstants.PROPERTIES_ROOT_PATH).replace("\\", "/")).append('/');
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

            final CommandOutput execData = applicationCommandService.secureCopyConfFile(jvm.getHostName(),
                    confFile.getAbsolutePath().replace("\\", "/"),
                    target.toString(),
                    new RuntimeCommandBuilder());
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
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateCommand command, User user) {
        command.validateCommand();
        final Event<UploadAppTemplateCommand> event = new Event<>(command, AuditEvent.now(user));
        return applicationPersistenceService.uploadAppTemplate(event);
    }

    @Override
    public String previewResourceTemplate(String appName, String groupName, String jvmName, String template) {
        final Map<String, Application> bindings = new HashMap<>();
        bindings.put("webApp", new WebApp(applicationDao.findApplication(appName, groupName, jvmName),
                jvmDao.findJvm(jvmName, groupName)));
        return GeneratorUtils.bindDataToTemplateText(bindings, template);
    }

    @Override
    public void copyApplicationWarToGroupHosts(Application application, RuntimeCommandBuilder rtCommandBuilder) {
        File applicationWar = new File(application.getWarPath());
        final String sourcePath = applicationWar.getParent();
        File tempWarFile = new File(sourcePath  + "/" + application.getWarName());
        try {
            FileCopyUtils.copy(applicationWar, tempWarFile);
            final String destPath = ApplicationProperties.get("stp.webapps.dir");
            Group group = groupService.getGroup(application.getGroup().getId());
            final Set<Jvm> theJvms = group.getJvms();
            if (theJvms != null && theJvms.size() > 0) {
                Set<String> hostNames = new HashSet<>();
                for (Jvm jvm : theJvms) {
                    final String host = jvm.getHostName();
                    if (hostNames.contains(host)){
                        continue;
                    } else {
                        hostNames.add(host);
                    }
                    rtCommandBuilder.reset();
                    rtCommandBuilder.setOperation(SCP_SCRIPT_NAME);
                    rtCommandBuilder.addCygwinPathParameter(tempWarFile.getAbsolutePath().replaceAll("\\\\", "/"));
                    rtCommandBuilder.addParameter(aemSshConfig.getSshConfiguration().getUserName());
                    rtCommandBuilder.addParameter(host);
                    rtCommandBuilder.addCygwinPathParameter(destPath);
                    RuntimeCommand rtCommand = rtCommandBuilder.build();

                    CommandOutput execData = rtCommand.execute();
                    if (execData.getReturnCode().wasSuccessful()) {
                        LOGGER.info("Copy of application war {} to {} was successful", applicationWar.getName(), host);
                    } else {
                        String errorOutput = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                        LOGGER.error("Copy of application war {} to {} FAILED::{}", applicationWar.getName(), host, errorOutput);
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to copy application war to the group host " + host);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Creation of temporary war file for {} FAILED :: {}", application.getWarPath(), e);
            throw new InternalErrorException(AemFaultType.INVALID_PATH, "Failed to create temporary war file for copying to remote hosts");
        } finally {
            if (tempWarFile.exists()) {
                tempWarFile.delete();
            }
        }
    }

    /**
     * As the name describes, this method creates the path if it does not exists.
     */
    private static void createPathIfItDoesNotExists(String path) {
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
    private File createConfFile(final String appName, final String groupName, final String jvmName,
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
                .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));

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

    /**
     * Inner class application wrapper to include a web application's parent JVM.
     */
    private class WebApp extends Application {
        final Jvm jvm;

        public WebApp(final Application app, final Jvm parentJvm) {
            super(app.getId(), app.getName(), app.getWarPath(), app.getWebAppContext(), app.getGroup(), app.isSecure(),
                    app.isLoadBalanceAcrossServers(), app.getWarName());
            jvm = parentJvm;
        }

        @SuppressWarnings("unused")
            // used by template bindings
        Jvm getJvm() {
            return jvm;
        }
    }

}
