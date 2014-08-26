package com.siemens.cto.aem.service.app;

import java.util.List;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    Application updateApplication(UpdateApplicationCommand anAppToUpdate, User user);
    Application createApplication(CreateApplicationCommand anAppToCreate, User user);
    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    List<Application> getApplications(PaginationParameter somePagination);

    List<Application> findApplications(Identifier<Group> groupId, PaginationParameter somePagination);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId, PaginationParameter somePagination);

    Application uploadWebArchive(UploadWebArchiveCommand command, User user);

    Application deleteWebArchive(Identifier<Application> appToRemoveWAR, User user);
}
