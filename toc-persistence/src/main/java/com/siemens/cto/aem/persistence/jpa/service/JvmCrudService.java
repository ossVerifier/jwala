package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;

import java.util.List;

public interface JvmCrudService extends CrudService<JpaJvm, Jvm> {

    JpaJvm createJvm(final Event<CreateJvmRequest> aJvmToCreate);

    JpaJvm updateJvm(final Event<UpdateJvmRequest> aJvmToUpdate);

    JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<JpaJvm> getJvms();

    List<JpaJvm> findJvms(final String aName);

    List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aGroupId);

    JpaJvmConfigTemplate uploadJvmTemplateXml(Event<UploadJvmTemplateRequest> event);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName);

    void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    Jvm findJvm(String jvmName, String groupName);

}
