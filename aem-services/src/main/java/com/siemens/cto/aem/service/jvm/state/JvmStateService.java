package com.siemens.cto.aem.service.jvm.state;

import java.util.Set;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmStateService {

    CurrentJvmState setCurrentJvmState(final SetJvmStateCommand aCommand,
                                       final User aUser);

    CurrentJvmState getCurrentJvmState(final Identifier<Jvm> aJvmId);

    Set<CurrentJvmState> getCurrentJvmStates(final Set<Identifier<Jvm>> someJvmIds);

    Set<CurrentJvmState> getCurrentJvmStates(final PaginationParameter somePagination);
}
