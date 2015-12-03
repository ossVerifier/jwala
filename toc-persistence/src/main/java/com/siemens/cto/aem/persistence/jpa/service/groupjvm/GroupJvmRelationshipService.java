package com.siemens.cto.aem.persistence.jpa.service.groupjvm;

import com.siemens.cto.aem.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;

import java.util.List;

public interface GroupJvmRelationshipService {

    void addJvmToGroup(final Event<AddJvmToGroupRequest> anEvent);

    void removeJvmFromGroup(final Event<RemoveJvmFromGroupRequest> anEvent);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);

    void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);
}
