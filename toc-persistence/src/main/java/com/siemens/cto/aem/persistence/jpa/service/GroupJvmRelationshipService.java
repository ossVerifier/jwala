package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;

import java.util.List;

public interface GroupJvmRelationshipService extends CrudService<JpaGroup> {

    void addJvmToGroup(final Event<AddJvmToGroupRequest> anEvent);

    void removeJvmFromGroup(final Event<RemoveJvmFromGroupRequest> anEvent);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);

    void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

}
