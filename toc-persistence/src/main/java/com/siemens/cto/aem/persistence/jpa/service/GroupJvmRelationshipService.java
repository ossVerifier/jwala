package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupJvmRelationshipService extends CrudService<JpaGroup> {

    void addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest);

    void removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest);

    void removeRelationshipsForGroup(final Identifier<Group> aGroupId);

    void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId);

    void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

}
