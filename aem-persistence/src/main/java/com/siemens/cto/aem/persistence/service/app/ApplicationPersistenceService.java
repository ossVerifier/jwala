package com.siemens.cto.aem.persistence.service.app;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;

public interface ApplicationPersistenceService {

    Application createApplication(final Event<CreateApplicationCommand> anAppToCreate);

    Application updateApplication(final Event<UpdateApplicationCommand> anAppToUpdate);

    Application updateWARPath(final Event<UploadWebArchiveCommand> anAppToUpdate, String warPath);

    void removeApplication(final Identifier<Application> anAppToRemove);

}
