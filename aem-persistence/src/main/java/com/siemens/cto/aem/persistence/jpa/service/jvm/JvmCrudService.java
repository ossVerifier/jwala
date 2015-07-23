package com.siemens.cto.aem.persistence.jpa.service.jvm;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

import java.util.List;

public interface JvmCrudService {

    JpaJvm createJvm(final Event<CreateJvmCommand> aJvmToCreate);

    JpaJvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate);

    JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<JpaJvm> getJvms();

    List<JpaJvm> findJvms(final String aName);

    List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aGroupId);

}
