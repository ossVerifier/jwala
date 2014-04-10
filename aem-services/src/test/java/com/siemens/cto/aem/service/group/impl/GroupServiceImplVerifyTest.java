package com.siemens.cto.aem.service.group.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.AddJvmsToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupServiceImplVerifyTest {

    private GroupServiceImpl impl;
    private GroupPersistenceService groupPersistenceService;
    private User user;
    private PaginationParameter pagination;

    @Before
    public void setUp() {

        groupPersistenceService = mock(GroupPersistenceService.class);
        impl = new GroupServiceImpl(groupPersistenceService);
        user = new User("unused");
        pagination = new PaginationParameter();
    }

    @Test
    public void testCreateGroup() {

        final CreateGroupCommand command = mock(CreateGroupCommand.class);

        impl.createGroup(command,
                         user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).createGroup(matchCommandInEvent(command));
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.getGroup(id);

        verify(groupPersistenceService, times(1)).getGroup(eq(id));
    }

    @Test
    public void testGetGroups() {

        impl.getGroups(pagination);

        verify(groupPersistenceService, times(1)).getGroups(eq(pagination));
    }

    @Test
    public void testFindGroups() {

        final String fragment = "unused";

        impl.findGroups(fragment,
                        pagination);

        verify(groupPersistenceService, times(1)).findGroups(eq(fragment),
                                                             eq(pagination));
    }

    @Test(expected = BadRequestException.class)
    public void testFindGroupsWithBadName() {

        final String badFragment = "";

        impl.findGroups(badFragment,
                        pagination);
    }

    @Test
    public void testUpdateGroup() {

        final UpdateGroupCommand command = mock(UpdateGroupCommand.class);

        impl.updateGroup(command,
                         user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).updateGroup(matchCommandInEvent(command));
    }

    @Test
    public void testRemoveGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.removeGroup(id);

        verify(groupPersistenceService, times(1)).removeGroup(eq(id));
    }

    @Test
    public void testAddJvmToGroup() {

        final AddJvmToGroupCommand command = mock(AddJvmToGroupCommand.class);

        impl.addJvmToGroup(command,
                           user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(command));
    }

    @Test
    public void testAddJvmsToGroup() {

        final AddJvmsToGroupCommand command = mock(AddJvmsToGroupCommand.class);

        final Set<AddJvmToGroupCommand> addCommands = createMockedAddCommands(5);
        when(command.toCommands()).thenReturn(addCommands);

        impl.addJvmsToGroup(command,
                            user);

        verify(command, times(1)).validateCommand();
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(addCommand, times(1)).validateCommand();
            verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(addCommand));
        }
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final RemoveJvmFromGroupCommand command = mock(RemoveJvmFromGroupCommand.class);

        impl.removeJvmFromGroup(command,
                                user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).removeJvmFromGroup(matchCommandInEvent(command));
    }

    protected Set<AddJvmToGroupCommand> createMockedAddCommands(final int aNumberToCreate) {

        final Set<AddJvmToGroupCommand> commands = new HashSet<>(aNumberToCreate);

        for (int i = 0; i < aNumberToCreate; i++) {
            commands.add(mock(AddJvmToGroupCommand.class));
        }

        return commands;
    }

    protected <T> Event<T> matchCommandInEvent(final T aCommand) {
        return argThat(new EventMatcher<T>(aCommand));
    }

    static class EventMatcher<T> extends ArgumentMatcher<Event<T>> {

        private final T expectedCommand;

        EventMatcher(final T theCommand) {
            expectedCommand = theCommand;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object argument) {
            final Event<T> event = (Event<T>)argument;
            return expectedCommand.equals(event.getCommand());
        }
    }
}
