package com.siemens.cto.aem.persistence.service.app;

import com.siemens.cto.aem.domain.command.app.*;
import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;

import java.util.List;

public interface ApplicationPersistenceService {

    Application createApplication(Event<CreateApplicationCommand> anAppToCreate, String appContextTemplate,
                                  String roleMappingPropertiesTemplate, String appPropertiesTemplate);

    Application updateApplication(final Event<UpdateApplicationCommand> anAppToUpdate);

    Application updateWARPath(final Event<UploadWebArchiveCommand> anAppToUpdate, String warPath);

    Application removeWARPath(final Event<RemoveWebArchiveCommand> anAppToUpdate);

    void removeApplication(final Identifier<Application> anAppToRemove);

    // Note: Do we really need a persistence service and a CRUD service ? Can we just have a DAO to make
    //       things simple ? TODO: Discuss this with the team in the future.
    List<String> getResourceTemplateNames(final String appName);

    String getResourceTemplate(final String appName, final String resourceTemplateName);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template);

    JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateCommand> event);
}
