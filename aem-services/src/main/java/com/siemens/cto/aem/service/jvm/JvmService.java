package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UploadJvmTemplateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;

import java.util.List;

public interface JvmService {

    Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                  final User aCreatingUser);

    Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsCommand aCreateAndAssignCommand,
                           final User aCreatingUser);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    Jvm getJvm(final String jvmName);

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aJvmNameFragment);

    List<Jvm> findJvms(final Identifier<Group> aJvmId);

    Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                  final User anUpdatingUser);

    void removeJvm(final Identifier<Jvm> aJvmId);

    String generateConfigFile(String aJvmName, String templateName);

    String performDiagnosis(Identifier<Jvm> aJvmId);

    CommandOutput secureCopyFile(RuntimeCommandBuilder runtimeCommandBuilder, String fileName, String srcDirPath, String destHostName, String destPath) throws CommandFailureException;

    JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateCommand command, User user);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    String generateInvokeBat(String jvmName);

    boolean isJvmStarted(Jvm jvm);

    String previewResourceTemplate(String jvmName,
                                   String groupName,
                                   String template);
}
