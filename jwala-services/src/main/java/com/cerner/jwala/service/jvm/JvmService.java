package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;

import java.util.List;

public interface JvmService {

    Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    Jvm getJvm(final String jvmName);

    List<Jvm> getJvms();

    Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, final User anUpdatingUser);

    void removeJvm(final Identifier<Jvm> aJvmId, User user);

    void deleteJvmWindowsService(ControlJvmRequest controlJvmRequest, Jvm jvm, User user);

    String generateConfigFile(String aJvmName, String templateName);

    Jvm generateAndDeployJvm(String jvmName, User user);

    Jvm generateAndDeployFile(String jvmName, String fileName, User user);

    String performDiagnosis(Identifier<Jvm> aJvmId);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    String generateInvokeBat(String jvmName);

    String previewResourceTemplate(String fileName, String jvmName, String groupName, String template);

    void updateState(Identifier<Jvm> id, JvmState state);

    /**
     * Ping's the JVM and updates its state.
     *
     * @param jvm the JVM
     */
    void pingAndUpdateJvmState(Jvm jvm);

    void deployApplicationContextXMLs(Jvm jvm, User user);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    /**
     * Create JVM default templates.
     *
     * @param jvmName
     * @param parentGroup
     */
    void createDefaultTemplates(String jvmName, Group parentGroup);

    String getResourceTemplateMetaData(String jvmName, String fileName);

    void checkForSetenvBat(String jvmName);

    /**
     * Delete a JVM
     * @param name the name of the JVM to delete
     * @param userName
     */
    void deleteJvm(String name, String userName);

}
