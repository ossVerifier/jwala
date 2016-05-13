package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    String previewResourceTemplate(String jvmName, String groupName, String template);

    void updateState(Identifier<Jvm> id, JvmState state);

    void updateState(Identifier<Jvm> id, JvmState state, String msg);

    /**
     * Ping's the JVM and updates its state.
     * @param jvm the JVM
     */
    void pingAndUpdateJvmState(Jvm jvm);

    void addAppTemplatesForJvm(Jvm jvm, Set<Identifier<Group>> groups);

    void deployApplicationContextXMLs(Jvm jvm);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    ResourceTemplateMetaData getResourceTemplateMetaData(String jvmName) throws IOException;

    /**
     * Combines data to the template to come up with resource file(s) which are saved in a file who's location
     * is determined by a path definition found in the meta data of the JVMs configuration template data
     * ({@link JpaJvmConfigTemplate}). This method generates all the resource files associated to a JVM as specified
     * by the JVM name.
     *
     * @param jvmName the JVM name
     * @param destPath the path where the file(s) are saved
     * @return the number of resource files generated
     * @throws IOException
     */
    int generateResourceFiles(String jvmName, String destPath) throws IOException;

    /**
     * Create JVM default templates.
     * @param jvmName
     */
    void createDefaultTemplates(String jvmName);
}
