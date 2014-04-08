package com.siemens.cto.aem.persistence.dao.groupjvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public interface GroupJvmRelationshipDao {

    void addJvmToGroup(final Event<AddJvmToGroupCommand> aJvmToAdd);

    void removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> aJvmToRemove);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);
}
