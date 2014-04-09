package com.siemens.cto.aem.persistence.jpa.service.jvm;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public interface JvmCrudService {

    JpaJvm createJvm(final Event<CreateJvmCommand> aJvmToCreate);

    JpaJvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate);

    JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<JpaJvm> getJvms(final PaginationParameter somePagination);

    List<JpaJvm> findJvms(final String aName,
                          final PaginationParameter somePagination);

    List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup,
                                     final PaginationParameter somePagination);

    void removeJvm(final Identifier<Jvm> aGroupId);

}
