package com.siemens.cto.aem.persistence.dao.app;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

import java.util.List;

public interface ApplicationDao {

//    Application createApplication(
//            final Event<CreateApplicationCommand> aApplicationToCreate);
//
//    Application updateApplication(
//            final Event<UpdateApplicationCommand> aApplicationToUpdate);
//
    Application getApplication(final Identifier<Application> aApplicationId)
            throws NotFoundException;

    List<Application> getApplications(final PaginationParameter somePagination);

    List<Application> findApplications(final String aGroupName,
            final PaginationParameter somePagination);

//    void removeApplication(final Identifier<Application> aApplicationId);
//
    List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId,
            PaginationParameter aPaginationParam);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId, PaginationParameter somePagination);

//    void removeApplicationsBelongingTo(final Identifier<Group> aGroupId);
//

    List<Application> findApplicationsBelongingToWebServer(String aWebServerName,
                                                           PaginationParameter somePagination);

}
