package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.ApplicationException;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UploadServerXmlTemplateCommand;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateGateway;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.template.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import groovy.text.SimpleTemplateEngine;
import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;
import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.SERVER_XML_TEMPLATE;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);

    private static final String DIAGNOSIS_INITIATED = "Diagnosis Initiated on JVM ${jvm.jvmName}, host ${jvm.hostName}";

    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final FileManager fileManager;
    private final JvmStateGateway jvmStateGateway;
    private ClientFactoryHelper clientFactoryHelper;
    private SshConfiguration sshConfig;

    public JvmServiceImpl(final JvmPersistenceService theJvmPersistenceService,
                          final GroupService theGroupService,
                          final FileManager theFileManager,
                          final JvmStateGateway theJvmStateGateway, ClientFactoryHelper factoryHelper,
                          final SshConfiguration sshConfig) {
        jvmPersistenceService = theJvmPersistenceService;
        groupService = theGroupService;
        fileManager = theFileManager;
        jvmStateGateway = theJvmStateGateway;
        clientFactoryHelper = factoryHelper;
        this.sshConfig = sshConfig;
    }

    @Override
    @Transactional
    public Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                         final User aCreatingUser) {

        aCreateJvmCommand.validateCommand();

        final Event<CreateJvmCommand> event = new Event<>(aCreateJvmCommand,
                AuditEvent.now(aCreatingUser));

        return jvmPersistenceService.createJvm(event);
    }

    @Override
    @Transactional
    public Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsCommand aCreateAndAssignCommand,
                                  final User aCreatingUser) {

        //The commands are validated in createJvm() and groupService.addJvmToGroup()

        final Jvm newJvm = createJvm(aCreateAndAssignCommand.getCreateCommand(),
                aCreatingUser);

        final Set<AddJvmToGroupCommand> addCommands = aCreateAndAssignCommand.toAddCommandsFor(newJvm.getId());
        addJvmToGroups(addCommands,
                aCreatingUser);

        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {

        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms() {

        return jvmPersistenceService.getJvms();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final String aJvmNameFragment) {

        new JvmNameRule(aJvmNameFragment).validate();
        return jvmPersistenceService.findJvms(aJvmNameFragment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final Identifier<Group> aGroupId) {

        return jvmPersistenceService.findJvmsBelongingTo(aGroupId);

    }

    @Override
    @Transactional
    public Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                         final User anUpdatingUser) {

        anUpdateJvmCommand.validateCommand();

        final Event<UpdateJvmCommand> event = new Event<>(anUpdateJvmCommand,
                AuditEvent.now(anUpdatingUser));

        jvmPersistenceService.removeJvmFromGroups(anUpdateJvmCommand.getId());

        addJvmToGroups(anUpdateJvmCommand.getAssignmentCommands(),
                anUpdatingUser);

        return jvmPersistenceService.updateJvm(event);
    }

    @Override
    @Transactional
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        jvmPersistenceService.removeJvm(aJvmId);
    }

    protected void addJvmToGroups(final Set<AddJvmToGroupCommand> someAddCommands,
                                  final User anAddingUser) {
        for (final AddJvmToGroupCommand command : someAddCommands) {
            groupService.addJvmToGroup(command,
                    anAddingUser);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String generateConfig(String aJvmName) {
        final List<Jvm> jvmList = jvmPersistenceService.findJvms(aJvmName);

        if (jvmList.size() == 1) {
            Jvm jvm = jvmList.get(0);
            try {
                final String serverXmlTemplateText = jvmPersistenceService.getJvmTemplate("server.xml", jvm.getId());
                return TomcatJvmConfigFileGenerator
                        .getServerXmlFromText(serverXmlTemplateText, jvm);
            } catch (BadRequestException bre) {
                // TODO remove once we know the server.xml template is in the database as part of the EPM install
                try {
                    return TomcatJvmConfigFileGenerator.getServerXmlFromFile(fileManager.getAbsoluteLocation(SERVER_XML_TEMPLATE), jvm);
                } catch (IOException e) {
                    throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Failed to find the template in the database or on the file system");
                }
            }
        }
        if (jvmList.size() > 1) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED, "Too many JVMs of the same name");
        } else {
            throw new BadRequestException(AemFaultType.JVM_NOT_FOUND, "JVM not found");
        }
    }

    @Override
    public String performDiagnosis(Identifier<Jvm> aJvmId) {

        // if the Jvm does not exist, we'll get a 404 NotFoundException
        Jvm jvm = jvmPersistenceService.getJvm(aJvmId);

        // this is a fire and forget gateway. There will be no response.
        jvmStateGateway.initiateJvmStateRequest(jvm);

        SimpleTemplateEngine engine = new SimpleTemplateEngine();
        HashMap<String, Object> binding = new HashMap<String, Object>();
        binding.put("jvm", jvm);

        try {
            String diagnosis = engine.createTemplate(DIAGNOSIS_INITIATED).make(binding).toString();
            return diagnosis;
        } catch (CompilationFailedException | ClassNotFoundException | IOException e) {
            throw new ApplicationException(DIAGNOSIS_INITIATED, e);
            // why do this? Because if there was a problem with the template that made
            // it past initial testing, then it is probably due to the jvm in the binding
            // so just dump out the diagnosis template and the exception so it can be 
            // debugged.
        }

    }

    @Override
    public ExecData secureCopyConfigTar(Jvm jvm, RuntimeCommandBuilder rtCommandBuilder) throws CommandFailureException {

        final String jvmName = jvm.getJvmName();

        // ping the web server and return if not stopped
        try {
            ClientHttpResponse response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            if (response.getStatusCode() == HttpStatus.OK) {
                return new ExecData(new ExecReturnCode(1), "", "The target JVM must be stopped before attempting to copy the tar config to the JVM");
            }
        } catch (IOException e) {
            if (!(e instanceof ConnectException || e instanceof ConnectTimeoutException)) {
                LOGGER.error("Failed to ping {} while attempting to copy tar config :: ERROR: {}", jvmName, e.getMessage());
                throw new InternalErrorException(AemFaultType.INVALID_JVM_OPERATION, "Failed to ping while attempting to copy tar config", e);
            }
        }

        // create and execute the scp command
        String jvmConfigTar = jvmName + "_config.tar";
        rtCommandBuilder.setOperation(SCP_SCRIPT_NAME);
        rtCommandBuilder.addCygwinPathParameter(ApplicationProperties.get("stp.jvm.resources.dir") + "/" + jvmConfigTar);
        rtCommandBuilder.addParameter(sshConfig.getUserName());
        rtCommandBuilder.addParameter(jvm.getHostName());
        rtCommandBuilder.addCygwinPathParameter(ApplicationProperties.get("paths.instances"));
        RuntimeCommand rtCommand = rtCommandBuilder.build();
        return rtCommand.execute();
    }

    @Override
    @Transactional
    public JpaJvmConfigTemplate uploadServerXml(UploadServerXmlTemplateCommand command, User user) {
        command.validateCommand();
        final Event<UploadServerXmlTemplateCommand> event = new Event<>(command, AuditEvent.now(user));
        return jvmPersistenceService.uploadServerXml(event);
    }

}
