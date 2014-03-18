package com.siemens.cto.aem.persistence.dao.jvm;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface JvmDao {

    Jvm createJvm(final Event<CreateJvmCommand> aJvmToCreate);

    Jvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate);

    Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<Jvm> getJvms(final PaginationParameter somePagination);

    List<Jvm> findJvms(final String aName,
                       final PaginationParameter somePagination);

    List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup,
                                  final PaginationParameter somePagination);

    void removeJvm(final Identifier<Jvm> aGroupId);

    void removeJvmsBelongingTo(final Identifier<Group> aGroupId);
}
