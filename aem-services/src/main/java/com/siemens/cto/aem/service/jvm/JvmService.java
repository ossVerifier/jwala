package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;

import java.util.List;

public interface JvmService {

    Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                  final User aCreatingUser);

    Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsCommand aCreateAndAssignCommand,
                           final User aCreatingUser);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aJvmNameFragment);

    List<Jvm> findJvms(final Identifier<Group> aJvmId);

    Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                  final User anUpdatingUser);

    void removeJvm(final Identifier<Jvm> aJvmId);

    String generateConfig(String aJvmName);

    String performDiagnosis(Identifier<Jvm> aJvmId);

    ExecData secureCopyConfigTar(Jvm jvm, RuntimeCommandBuilder runtimeCommandBuilder) throws CommandFailureException;
}
