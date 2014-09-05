package com.siemens.cto.aem.service.jvm;

import java.util.List;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmService {

    Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                  final User aCreatingUser);

    Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsCommand aCreateAndAssignCommand,
                           final User aCreatingUser);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    List<Jvm> getJvms(final PaginationParameter aPaginationParam);

    List<Jvm> findJvms(final String aJvmNameFragment,
                       final PaginationParameter aPaginationParam);

    List<Jvm> findJvms(final Identifier<Group> aJvmId,
                       final PaginationParameter aPaginationParam);

    Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                  final User anUpdatingUser);

    void removeJvm(final Identifier<Jvm> aJvmId);

    String generateConfig(String aJvmName);

}
