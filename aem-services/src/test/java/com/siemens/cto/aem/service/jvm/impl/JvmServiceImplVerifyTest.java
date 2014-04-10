package com.siemens.cto.aem.service.jvm.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmServiceImplVerifyTest {

    private JvmServiceImpl impl;
    private JvmPersistenceService jvmPersistenceService;
    private GroupService groupService;
    private User user;

    @Before
    public void setup() {
        jvmPersistenceService = mock(JvmPersistenceService.class);
        groupService = mock(GroupService.class);
        user = new User("unused");
        impl = new JvmServiceImpl(jvmPersistenceService,
                                  groupService);
    }

    @Test
    public void testCreateValidate() {

        final CreateJvmCommand command = mock(CreateJvmCommand.class);

        impl.createJvm(command,
                       user);

        verify(command, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).createJvm(Matchers.<Event<CreateJvmCommand>>anyObject());
    }

    @Test
    public void testCreateValidateAdd() {

        final CreateJvmAndAddToGroupsCommand command = mock(CreateJvmAndAddToGroupsCommand.class);
        final Jvm jvm = mockJvmWithId(new Identifier<Jvm>(-123456L));
        final Set<AddJvmToGroupCommand> addCommands = createMockedAddJvmToGroupCommands(3);

        when(command.getAssignmentCommandsFor(eq(jvm.getId()))).thenReturn(addCommands);
        when(jvmPersistenceService.createJvm(Matchers.<Event<CreateJvmCommand>>anyObject())).thenReturn(jvm);

        impl.createAndAssignJvm(command,
                                user);

        verify(command, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).createJvm(Matchers.<Event<CreateJvmCommand>>anyObject());
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(argThat(new CommandMatcher<AddJvmToGroupCommand>(addCommand)),
                                                         eq(user));
        }
    }

    @Test
    public void testUpdateJvmShouldValidateCommand() {

        final UpdateJvmCommand command = mock(UpdateJvmCommand.class);
        final Set<AddJvmToGroupCommand> addCommands = createMockedAddJvmToGroupCommands(5);

        when(command.getAssignmentCommands()).thenReturn(addCommands);

        impl.updateJvm(command,
                       user);

        verify(command, times(1)).validateCommand();
        verify(jvmPersistenceService, times(1)).updateJvm(Matchers.<Event<UpdateJvmCommand>>anyObject());
        verify(jvmPersistenceService,
               times(1)).removeJvmFromGroups(Matchers.<Identifier<Jvm>>anyObject());
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(groupService, times(1)).addJvmToGroup(argThat(new CommandMatcher<AddJvmToGroupCommand>(addCommand)),
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
        final PaginationParameter pagination = new PaginationParameter();

        impl.findJvms(fragment,
                      pagination);

        verify(jvmPersistenceService, times(1)).findJvms(eq(fragment),
                                                         eq(pagination));
    }

    @Test(expected = BadRequestException.class)
    public void testFindByInvalidName() {

        final String badFragment = "";
        final PaginationParameter pagination = new PaginationParameter();

        impl.findJvms(badFragment,
                      pagination);
    }

    @Test
    public void testFindByGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);
        final PaginationParameter pagination = new PaginationParameter();

        impl.findJvms(id,
                      pagination);

        verify(jvmPersistenceService, times(1)).findJvmsBelongingTo(eq(id),
                                                                    eq(pagination));
    }

    @Test
    public void testGetAll() {

        final PaginationParameter pagination = new PaginationParameter();

        impl.getJvms(pagination);

        verify(jvmPersistenceService, times(1)).getJvms(eq(pagination));
    }

    @Test
    public void testGetSpecific() {

        final Identifier<Jvm> id = new Identifier<>(-123456L);

        impl.getJvm(id);

        verify(jvmPersistenceService, times(1)).getJvm(eq(id));
    }

    protected Set<AddJvmToGroupCommand> createMockedAddJvmToGroupCommands(final int aNumberToCreate) {
        final Set<AddJvmToGroupCommand> commands = new HashSet<>();
        for (int i = 0; i < aNumberToCreate; i++) {
            commands.add(mock(AddJvmToGroupCommand.class));
        }
        return commands;
    }

    protected Jvm mockJvmWithId(final Identifier<Jvm> anId) {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getId()).thenReturn(anId);
        return jvm;
    }

    static class EventCommandAndUserMatcher<T> extends ArgumentMatcher<Event<T>> {

        private final T expectedCommand;
        private final String expectedUserId;

        EventCommandAndUserMatcher(final T theExpectedCommand,
                                   final User theExpectedUser) {
            expectedCommand = theExpectedCommand;
            expectedUserId = theExpectedUser.getId();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object argument) {
            final T actualCommand = ((Event<T>)argument).getCommand();
            final String actualUserId = ((Event<T>)argument).getAuditEvent().getUser().getUserId();

            return (expectedCommand.equals(actualCommand)) && (expectedUserId.equals(actualUserId));
        }
    }

    static class CommandMatcher<T> extends ArgumentMatcher<T> {

        private final Command expectedCommand;

        CommandMatcher(final Command theExpectedCommand) {
            expectedCommand = theExpectedCommand;
        }

        @Override
        public boolean matches(final Object argument) {
            return expectedCommand.equals(argument);
        }
    }
}
