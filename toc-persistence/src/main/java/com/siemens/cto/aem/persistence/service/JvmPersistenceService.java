package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;

import java.util.List;

// TODO: Get rid of this...stick with the DAO
public interface JvmPersistenceService {

    Jvm createJvm(CreateJvmRequest createJvmRequest);

    Jvm updateJvm(UpdateJvmRequest updateJvmRequest);

    Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    JpaJvm getJpaJvm(Identifier<Jvm> aJvmId, boolean fetchGroups);

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aName);

    List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aJvmId);

    Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId);

    JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    // Note: Do we really need a persistence service and a CRUD service ? Can we just have a DAO to make
    //       things simple ? TODO: Discuss this with the team in the future.
    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    Jvm findJvm(String jvmName, String groupName);

    Jvm findJvmByExactName(String jvmName);

    void updateState(Identifier<Jvm> id, JvmState state);

    void updateErrorStatus(Identifier<Jvm> id, String errorSatus);

    void updateState(Identifier<Jvm> id, JvmState state, String errorStatus);

    List<Group> findGroupsByJvm(Identifier<Jvm> id);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    int removeTemplate(String name);

    int removeTemplate(String jvmName, String templateName);

    List<JpaJvmConfigTemplate> getConfigTemplates(String jvmName);

    JpaJvmConfigTemplate getConfigTemplate(String jvmName);

    List<JpaJvm> getJvmsByGroupId(String groupName);

    /**
     * This service returns a list of JpaJvm objects which belong to a particular group. This method uses the group name to lookup the JpaJvms.
     * @param groupName
     * @return a list of JpaJvm objects
     */
    List<Jvm> getJvmsByGroupName(String groupName);
}
