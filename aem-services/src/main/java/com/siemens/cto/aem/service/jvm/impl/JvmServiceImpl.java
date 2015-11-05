package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UploadJvmTemplateCommand;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.JvmStateGateway;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.template.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);

    private static final String DIAGNOSIS_INITIATED = "Diagnosis Initiated on JVM ${jvm.jvmName}, host ${jvm.hostName}";

    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final FileManager fileManager;
    private final StateService<Jvm, JvmState> stateService;
    private SshConfiguration sshConfig;

    public JvmServiceImpl(final JvmPersistenceService theJvmPersistenceService,
                          final GroupService theGroupService,
                          final FileManager theFileManager,
                          final JvmStateGateway theJvmStateGateway, ClientFactoryHelper factoryHelper,
                          final StateService<Jvm, JvmState> theJvmStateService, final SshConfiguration theSshConfig) {
        jvmPersistenceService = theJvmPersistenceService;
        groupService = theGroupService;
        fileManager = theFileManager;
        stateService = theJvmStateService;
        sshConfig = theSshConfig;
    }

    @Override
    @Transactional
    public Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                         final User aCreatingUser) {

        aCreateJvmCommand.validateCommand();
        final Event<CreateJvmCommand> event = new Event<>(aCreateJvmCommand,
                AuditEvent.now(aCreatingUser));
        Jvm jvm = jvmPersistenceService.createJvm(event);
        // TODO add JVM_NEW state to JVM state table (already done? Appears as NEW on UI)

        return jvm;
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
    public Jvm getJvm(final String jvmName) {
        return jvmPersistenceService.findJvms(jvmName).get(0);
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
    public String generateConfigFile(String aJvmName, String templateName) {
        final List<Jvm> jvmList = jvmPersistenceService.findJvms(aJvmName);

        if (jvmList.size() == 1) {
            Jvm jvm = jvmList.get(0);
            final String serverXmlTemplateText = jvmPersistenceService.getJvmTemplate(templateName, jvm.getId());
            if (!serverXmlTemplateText.isEmpty()) {
                return TomcatJvmConfigFileGenerator
                        .getJvmConfigFromText(serverXmlTemplateText, jvm, jvmPersistenceService.getJvms());
            } else {
                throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND, "Failed to find the template in the database or on the file system");
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
        throw new UnsupportedOperationException("JVM diagnosis is not supported for now.");
    }

    @Override
    public ExecData secureCopyFile(RuntimeCommandBuilder rtCommandBuilder, String fileName, String srcDirPath, String destHostName, String destPath) throws CommandFailureException {

        rtCommandBuilder.setOperation(SCP_SCRIPT_NAME);
        rtCommandBuilder.addCygwinPathParameter(srcDirPath + "/" + fileName);
        rtCommandBuilder.addParameter(sshConfig.getUserName());
        rtCommandBuilder.addParameter(destHostName);
        rtCommandBuilder.addCygwinPathParameter(destPath);
        RuntimeCommand rtCommand = rtCommandBuilder.build();
        return rtCommand.execute();
    }

    public boolean isJvmStarted(Jvm jvm) {
        
        CurrentState<Jvm, JvmState> jvmCurrentState = stateService.getCurrentState(jvm.getId());
        JvmState jvmState = jvmCurrentState.getState();
        return jvmState.isStartedState();        
    }

    @Override
    public String previewResourceTemplate(String jvmName, String groupName, String template) {
        // TODO: Jvm name shouldn't be unique therefore we will have to use the groupName parameter in the future.
        return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template,
                                                                 jvmPersistenceService.findJvms(jvmName).get(0),
                                                                 jvmPersistenceService.getJvms());
    }

    @Override
    @Transactional
    public JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateCommand command, User user) {
        command.validateCommand();
        final Event<UploadJvmTemplateCommand> event = new Event<>(command, AuditEvent.now(user));
        return jvmPersistenceService.uploadJvmTemplateXml(event);
    }

    @Override
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmPersistenceService.getResourceTemplateNames(jvmName);
    }

    @Override
    public String getResourceTemplate(final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = jvmPersistenceService.getResourceTemplate(jvmName, resourceTemplateName);
        if (tokensReplaced) {
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template, jvmPersistenceService.findJvms(jvmName).get(0), jvmPersistenceService.getJvms());
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        return jvmPersistenceService.updateResourceTemplate(jvmName, resourceTemplateName, template);
    }

    @Override
    public String generateInvokeBat(String jvmName) {
        final List<Jvm> jvmList = jvmPersistenceService.findJvms(jvmName);

        if (jvmList.size() == 1) {
            Jvm jvm = jvmList.get(0);
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(fileManager.getResourceTypeTemplate("InvokeBat"), jvm, jvmPersistenceService.getJvms());
        }
        if (jvmList.size() > 1) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED, "Too many JVMs of the same name");
        } else {
            throw new BadRequestException(AemFaultType.JVM_NOT_FOUND, "JVM not found");
        }
    }

}
