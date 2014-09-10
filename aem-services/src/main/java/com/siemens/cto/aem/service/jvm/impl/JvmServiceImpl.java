package com.siemens.cto.aem.service.jvm.impl;

import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.SERVER_XML_TEMPLATE;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.toc.files.TemplateManager;

public class JvmServiceImpl implements JvmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);

    private final JvmPersistenceService jvmPersistenceService;
    private final GroupService groupService;
    private final TemplateManager templateManager;


    public JvmServiceImpl(final JvmPersistenceService theJvmPersistenceService,
                          final GroupService theGroupService, 
                          final TemplateManager theTemplateManager) {
        jvmPersistenceService = theJvmPersistenceService;
        groupService = theGroupService;
        templateManager = theTemplateManager;
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
    public List<Jvm> getJvms(final PaginationParameter aPaginationParam) {

        return jvmPersistenceService.getJvms(aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final String aJvmNameFragment,
                              final PaginationParameter aPaginationParam) {

        new JvmNameRule(aJvmNameFragment).validate();
        return jvmPersistenceService.findJvms(aJvmNameFragment,
                                              aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final Identifier<Group> aGroupId,
                              final PaginationParameter aPaginationParam) {

        return jvmPersistenceService.findJvmsBelongingTo(aGroupId,
                                                         aPaginationParam);

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
        final List<Jvm> jvm = jvmPersistenceService.findJvms(aJvmName, PaginationParameter.all());

        if(jvm.size()==1) { 
            try {
                return TomcatJvmConfigFileGenerator
                            .getServerXml(templateManager.getAbsoluteLocation(SERVER_XML_TEMPLATE), jvm.get(0));
            } catch(IOException e) {
                LOGGER.warn("Template not found", e);
                throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());                
            }
        } if(jvm.size() > 1) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED, "Too many JVMs of the same name");
        } else { 
            throw new BadRequestException(AemFaultType.JVM_NOT_FOUND, "JVM not found");
        }
    }

}
