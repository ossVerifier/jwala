package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JvmService {

    Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    Jvm getJvm(final String jvmName);

    List<Jvm> getJvms();

    Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword);

    void removeJvm(final Identifier<Jvm> aJvmId, User user);

    void deleteJvmWindowsService(ControlJvmRequest controlJvmRequest, Jvm jvm, User user);

    Jvm generateAndDeployJvm(String jvmName, User user);

    Jvm generateAndDeployFile(String jvmName, String fileName, User user);

    String performDiagnosis(Identifier<Jvm> aJvmId, User user);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

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
     * Generates all the required templates for the required jvm, and returns the source location and the destination
     * location.
     *
     * @param jvmName Name of the jvm for which the templates need to be generated.
     * @return a map with the key as the absolute location of the source file and the value
     * as the absolute location of the destination file.
     * @throws IOException
     */
    Map<String, String> generateResourceFiles(String jvmName) throws IOException;

    /**
     * Create JVM default templates.
     *
     * @param jvmName
     * @param parentGroup
     */
    void createDefaultTemplates(String jvmName, Group parentGroup);

    void checkForSetenvBat(String jvmName);

    /**
     * Delete a JVM
     * @param name the name of the JVM to delete
     * @param userName
     */
    void deleteJvm(String name, String userName);

}
