package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;

import java.util.List;

// TODO: Get rid of this...stick with the DAO
public interface JvmPersistenceService {

    Jvm createJvm(final Event<CreateJvmRequest> aJvmToCreate);

    Jvm updateJvm(final Event<UpdateJvmRequest> aJvmToUpdate);

    Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    JpaJvm getJpaJvm(Identifier<Jvm> aJvmId, boolean fetchGroups);

    List<Jvm> getJvms();

    List<Jvm> findJvms(final String aName);

    List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup);

    void removeJvm(final Identifier<Jvm> aJvmId);

    Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId);

    JpaJvmConfigTemplate uploadJvmTemplateXml(Event<UploadJvmTemplateRequest> event);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    // Note: Do we really need a persistence service and a CRUD service ? Can we just have a DAO to make
    //       things simple ? TODO: Discuss this with the team in the future.
    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

}
