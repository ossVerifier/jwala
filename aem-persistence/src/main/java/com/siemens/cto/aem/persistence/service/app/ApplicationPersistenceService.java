package com.siemens.cto.aem.persistence.service.app;

import com.siemens.cto.aem.domain.model.app.*;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;

public interface ApplicationPersistenceService {

    Application createApplication(final Event<CreateApplicationCommand> anAppToCreate);

    Application updateApplication(final Event<UpdateApplicationCommand> anAppToUpdate);

    Application updateWARPath(final Event<UploadWebArchiveCommand> anAppToUpdate, String warPath);

    Application removeWARPath(final Event<RemoveWebArchiveCommand> anAppToUpdate);

    void removeApplication(final Identifier<Application> anAppToRemove);

}
