package com.siemens.cto.aem.persistence.jpa.service;

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

public interface JvmCrudService extends CrudService<JpaJvm> {

    JpaJvm createJvm(CreateJvmRequest createJvmRequest);

    JpaJvm updateJvm(UpdateJvmRequest updateJvmRequest);

    JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<JpaJvm> getJvms();

    List<JpaJvm> findJvms(final String aName);

    List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aGroupId);

    JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName);

    void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    Jvm findJvm(String jvmName, String groupName);

    int updateState(Identifier<Jvm> id, JvmState state);

    int updateErrorStatus(Identifier<Jvm> id, String errorStatus);

    int updateState(Identifier<Jvm> id, JvmState state, String errorStatus);

    Jvm findJvmByExactName(String jvmName);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    int removeTemplate(String name);

    @Deprecated
    int removeTemplate(String jvmName, String templateName);

    List<JpaJvmConfigTemplate> getConfigTemplates(String jvmName);

    List<JpaJvm> getJvmsByGroupId(String groupName);

    List<Jvm> getJvmsByGroupName(String groupName);

    String getResourceTemplateMetaData(String jvmName, String fileName);

    boolean checkJvmResourceFileName(String groupName, String jvmName, String fileName);
}
