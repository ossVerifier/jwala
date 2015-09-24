package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.exception.ApplicationServiceException;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.template.GeneratorUtils;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationServiceImpl implements ApplicationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private static final String APP_CONF_PATH = "paths.app.conf";
    private static final String JVM_INSTANCE_DIR = "paths.instances";
    private static final String TEMP_DIR = "stp.temp.dir";
    private static final String TEMPLATE_PATH = "paths.templates";
    private static final String DEFAULT_APP_CONTEXT = "stp.default.app.context";

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
    
    public ApplicationServiceImpl(final ApplicationDao applicationDao,
                                  final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final ClientFactoryHelper clientFactoryHelper,
                                  final ApplicationCommandService applicationCommandService,
                                  final JvmDao jvmDao) {
        this.applicationDao = applicationDao;
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.applicationCommandService = applicationCommandService;
        this.jvmDao = jvmDao;
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

        // Get default template
        try {
            final Path path = new File(ApplicationProperties.get(TEMPLATE_PATH) + "\\" +
                                       ApplicationProperties.get(DEFAULT_APP_CONTEXT)).toPath();
            final String appContext = new String(Files.readAllBytes(path));
            return applicationPersistenceService.createApplication(event, appContext);
        } catch (IOException ioe) {
            throw new ApplicationServiceException("Error creating the application!", ioe);
        }
    }

    @Transactional
    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        applicationPersistenceService.removeApplication(anAppIdToRemove);
    }

    /** Non-transactional entry point, utilizes {@link PrivateApplicationServiceImpl}*/
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
        
        if(result.getType() == Type.DELETED) {
            return applicationPersistenceService.removeWARPath(event);
        }
        else {
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
            return GeneratorUtils.bindDataToTemplateText(bindings, template);
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template) {
        return applicationPersistenceService.updateResourceTemplate(appName, resourceTemplateName, template);
    }

    @Override
    @Transactional(readOnly = true)
    // TODO: Have an option to do a hot deploy or not.
    public ExecData deployConf(final String appName, final String groupName, final String jvmName,
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

            final StringBuilder target = new StringBuilder();
            target.append(ApplicationProperties.get(JVM_INSTANCE_DIR))
                  .append('/')
                  .append(jvmName)
                  .append('/')
                  .append(ApplicationProperties.get(APP_CONF_PATH))
                  .append('/')
                  .append(resourceTemplateName);

            final ExecData execData = applicationCommandService.secureCopyConfFile(jvm.getHostName(),
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
        } catch (FileNotFoundException fileNotFoundEx) {
            throw new DeployApplicationConfException(fileNotFoundEx);
        } catch (CommandFailureException cfe) {
            throw new DeployApplicationConfException(cfe);
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

    /**
     * Create application configuration file.
     *
     * @param appName - the application name.
     * @param groupName - the group where the application belongs to.
     * @param jvmName - the JVM name where the application is deployed.
     * @param resourceTemplateName - the name of the resource to generate.
     * @return the configuration file.
     */
    private File createConfFile(final String appName, final String groupName, final String jvmName,
                                final String resourceTemplateName)
            throws FileNotFoundException {
        PrintWriter out = null;
        final StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(ApplicationProperties.get(TEMP_DIR))
                       .append('/')
                       .append(appName)
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
                  app.isLoadBalanceAcrossServers());
            jvm = parentJvm;
        }

        Jvm getJvm() {
            return jvm;
        }
    }

}
