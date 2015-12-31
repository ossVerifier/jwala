package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

import java.util.List;

public interface ApplicationCrudService extends CrudService<JpaApplication> {

    JpaApplication createApplication(final Event<CreateApplicationRequest> anAppToCreate, JpaGroup jpaGroup);

    JpaApplication updateApplication(final Event<UpdateApplicationRequest> anAppToUpdate, JpaApplication jpaApp, JpaGroup jpaGroup);

    void removeApplication(final Identifier<Application> anAppId);

    JpaApplication getExisting(final Identifier<Application> anAppId);

    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, final String resourceTemplateName, JpaJvm jvm);

    void updateResourceTemplate(final String appName, final String resourceTemplateName, String template, JpaJvm jvm);

    void createConfigTemplate(JpaApplication app, String resourceTemplateName, String resourceTemplateContent, JpaJvm jvm);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest);

    Application getApplication(final Identifier<Application> aApplicationId) throws NotFoundException;

    List<Application> getApplications();

    List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId);

    Application findApplication(String appName, String groupName, String jvmName);

}
