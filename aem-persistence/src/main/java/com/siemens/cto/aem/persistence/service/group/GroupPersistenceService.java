package com.siemens.cto.aem.persistence.service.group;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.UploadJvmTemplateCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;

import java.util.List;
import java.util.Set;

public interface GroupPersistenceService extends StatePersistenceService<Group, GroupState> {

    Group createGroup(final Event<CreateGroupCommand> anEvent);

    Group updateGroup(final Event<UpdateGroupCommand> anEvent) throws NotFoundException;

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroup(final String name) throws NotFoundException;

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group addJvmToGroup(final Event<AddJvmToGroupCommand> anEvent) throws NotFoundException;

    Group removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> anEvent) throws NotFoundException;

    Group updateGroupStatus(Event<SetGroupStateCommand> aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException;

    Set<CurrentState<Group, GroupState>> getAllKnownStates();

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateCommand> uploadJvmTemplateCommands, User user, boolean overwriteExisting);
}
