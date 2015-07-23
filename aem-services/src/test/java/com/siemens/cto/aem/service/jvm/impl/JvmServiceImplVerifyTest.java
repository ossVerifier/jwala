package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmStateGateway;
import com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate;
import com.siemens.cto.toc.files.FileManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JvmServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmServiceImpl impl;
    private JvmStateGateway jvmStateGateway;
    private JvmPersistenceService jvmPersistenceService;
    private GroupService groupService;
    private User user;
    private FileManager fileManager;

    @Before
    public void setup() {
        jvmPersistenceService = mock(JvmPersistenceService.class);
        groupService = mock(GroupService.class);
        jvmStateGateway = mock(JvmStateGateway.class);
        user = new User("unused");
        fileManager = mock(FileManager.class);
        impl = new JvmServiceImpl(jvmPersistenceService, groupService, fileManager, jvmStateGateway);
    }

    @Test
    public void testCreateValidate() {

        final CreateJvmCommand command = mock(CreateJvmCommand.class);

        impl.createJvm(command,
                       user);

        verify(command, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).createJvm(matchCommandInEvent(command));
    }

    @Test
    public void testCreateValidateAdd() {

        final CreateJvmCommand createCommand = mock(CreateJvmCommand.class);
        final CreateJvmAndAddToGroupsCommand command = mock(CreateJvmAndAddToGroupsCommand.class);
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(-123456L));
        final Set<AddJvmToGroupCommand> addCommands = createMockedAddCommands(3);

        when(command.toAddCommandsFor(eq(jvm.getId()))).thenReturn(addCommands);
        when(command.getCreateCommand()).thenReturn(createCommand);
        when(jvmPersistenceService.createJvm(matchCommandInEvent(createCommand))).thenReturn(jvm);

        impl.createAndAssignJvm(command,
                                user);

        verify(createCommand, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).createJvm(matchCommandInEvent(createCommand));
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                                                         eq(user));
        }
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmCommand command = mock(UpdateJvmCommand.class);
        final Set<AddJvmToGroupCommand> addCommands = createMockedAddCommands(5);

        when(command.getAssignmentCommands()).thenReturn(addCommands);

        impl.updateJvm(command,
                       user);

        verify(command, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).updateJvm(matchCommandInEvent(command));
        verify(jvmPersistenceService, times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(matchCommand(addCommand),
                                                         eq(user));
        }
    }

    @Test
    public void testRemoveJvm() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        impl.removeJvm(id);

        verify(jvmPersistenceService, times(1)).removeJvm(eq(id));
    }

    @Test
    public void testFindByName() {

        final String fragment = "unused";

        impl.findJvms(fragment);

        verify(jvmPersistenceService, times(1)).findJvms(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindByInvalidName() {

        final String badFragment = "";

        impl.findJvms(badFragment);
    }

    @Test
    public void testFindByGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.findJvms(id);

        verify(jvmPersistenceService, times(1)).findJvmsBelongingTo(eq(id));
    }

    @Test
    public void testGetAll() {

        impl.getJvms();

        verify(jvmPersistenceService, times(1)).getJvms();
    }
    

    @Test
    public void testGenerateConfig() throws IOException {

        final Jvm jvm = new Jvm(new Identifier<Jvm>(-123456L), 
                "jvm-name", "host-name", new HashSet<LiteGroup>(),  80, 443, 443, 8005, 8009, new Path("/"),
                "EXAMPLE_OPTS=%someEnv%/someVal");
        final ArrayList<Jvm> jvms = new ArrayList<>(1);
        jvms.add(jvm);
        
        when(jvmPersistenceService.findJvms(eq(jvm.getJvmName()))).thenReturn(jvms);
        when(fileManager.getAbsoluteLocation(eq(ConfigurationTemplate.SERVER_XML_TEMPLATE))).thenReturn("/server-xml.tpl");
        impl.generateConfig(jvm.getJvmName());

        verify(fileManager, times(1)).getAbsoluteLocation(eq(ConfigurationTemplate.SERVER_XML_TEMPLATE));
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        impl.getJvm(id);

        verify(jvmPersistenceService, times(1)).getJvm(eq(id));
    }

    protected Jvm mockJvmWithId(final Identifier<Jvm> anId) {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(anId);
        return jvm;
    }
}
