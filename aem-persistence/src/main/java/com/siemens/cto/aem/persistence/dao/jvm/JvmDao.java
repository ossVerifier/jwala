package com.siemens.cto.aem.persistence.dao.jvm;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;

import java.util.List;

public interface JvmDao {

    Jvm createJvm(final Event<CreateJvmCommand> aJvmToCreate);

    Jvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate);

    Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aName);

    List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aGroupId);

    void removeJvmsBelongingTo(final Identifier<Group> aGroupId);

    Jvm findJvm(String jvmName, String groupName);
}
