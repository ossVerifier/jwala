package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UploadJvmTemplateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.command.UploadWebServerTemplateCommand;

import java.util.List;

public interface GroupService {

    Group createGroup(final CreateGroupCommand aCreateGroupCommand,
                      final User aCreatingUser);

    Group getGroup(final Identifier<Group> aGroupId);

    Group getGroupWithWebServers(final Identifier<Group> aGroupId);

    Group getGroup(final String name);

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aGroupNameFragment);

    Group updateGroup(final UpdateGroupCommand anUpdateGroupCommand,
                      final User anUpdatingUser);

    void removeGroup(final Identifier<Group> aGroupId);

    Group addJvmToGroup(final AddJvmToGroupCommand aCommand,
                        final User anAddingUser);

    Group addJvmsToGroup(final AddJvmsToGroupCommand aCommand,
                         final User anAddingUser);

    Group removeJvmFromGroup(final RemoveJvmFromGroupCommand aCommand,
                             final User aRemovingUser);

    /**
     * Gets the connection details of JVMs under a group specified by id.
     * @param id the group id
     * @return JVMs that are members of more than one group.
     */
    List<Jvm> getOtherGroupingDetailsOfJvms(final Identifier<Group> id);

    /**
     * Gets the connection details of Web Servers under a group specified by id.
     * @param id the group id
     * @return Web Servers that are members of more than one group.
     */
    List<WebServer> getOtherGroupingDetailsOfWebServers(final Identifier<Group> id);

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateCommand> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    Group populateWebServerConfig(Identifier<Group> aGroupId, List<UploadWebServerTemplateCommand> uploadWSTemplateCommands, User user, boolean overwriteExisting);
}
