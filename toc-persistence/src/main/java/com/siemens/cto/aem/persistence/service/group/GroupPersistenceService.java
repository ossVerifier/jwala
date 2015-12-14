package com.siemens.cto.aem.persistence.service.group;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;

import java.util.List;
import java.util.Set;

public interface GroupPersistenceService extends StatePersistenceService<Group, GroupState> {

    Group createGroup(final Event<CreateGroupRequest> anEvent);

    Group updateGroup(final Event<UpdateGroupRequest> anEvent) throws NotFoundException;

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroup(final String name) throws NotFoundException;

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    void removeGroup(String name) throws NotFoundException;

    Group addJvmToGroup(final Event<AddJvmToGroupRequest> anEvent) throws NotFoundException;

    Group removeJvmFromGroup(final Event<RemoveJvmFromGroupRequest> anEvent) throws NotFoundException;

    Group updateGroupStatus(Event<SetGroupStateRequest> aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException;

    Set<CurrentState<Group, GroupState>> getAllKnownStates();

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);
}
