package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;

import java.util.List;

public interface JvmService {

    Jvm createJvm(final CreateJvmRequest aCreateJvmRequest, final User aCreatingUser);

    Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, final User aCreatingUser);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    JpaJvm getJpaJvm(final Identifier<Jvm> aJvmId, boolean fetchGroups);

    Jvm getJvm(final String jvmName);

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aJvmNameFragment);

    List<Jvm> findJvms(final Identifier<Group> groupId);

    Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, final User anUpdatingUser);

    void removeJvm(final Identifier<Jvm> aJvmId);

    String generateConfigFile(String aJvmName, String templateName);

    String performDiagnosis(Identifier<Jvm> aJvmId);

    JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest, User user);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    String generateInvokeBat(String jvmName);

    boolean isJvmStarted(Jvm jvm);

    String previewResourceTemplate(String jvmName, String groupName, String template);

    void updateState(Identifier<Jvm> id, JvmState state);

    void updateState(Identifier<Jvm> id, JvmState state, String msg);

    /**
     * Ping's the JVM and updates its state.
     * @param jvm the JVM
     */
    void pingAndUpdateJvmState(Jvm jvm);

}
