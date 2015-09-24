package com.siemens.cto.aem.persistence.jpa.service.groupjvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UploadJvmTemplateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

import java.util.List;

public interface GroupJvmRelationshipService {

    void addJvmToGroup(final Event<AddJvmToGroupCommand> anEvent);

    void removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> anEvent);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);

    void populateJvmConfig(List<UploadJvmTemplateCommand> uploadJvmTemplateCommands, User user, boolean overwriteExisting);
}
