package com.siemens.cto.aem.persistence.jpa.service.app;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadAppTemplateCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface ApplicationCrudService {

    JpaApplication createApplication(final Event<CreateApplicationCommand> anAppToCreate, JpaGroup jpaGroup);

    JpaApplication updateApplication(final Event<UpdateApplicationCommand> anAppToUpdate, JpaApplication jpaApp, JpaGroup jpaGroup);

    void removeApplication(final Identifier<Application> anAppId);
    
    JpaApplication getExisting(final Identifier<Application> anAppId);

    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, final String resourceTemplateName);

    void updateResourceTemplate(final String appName, final String resourceTemplateName, String template);

    void createConfigTemplate(JpaApplication app, String resourceTemplateName, String resourceTemplateContent);

    JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateCommand> event);
}
