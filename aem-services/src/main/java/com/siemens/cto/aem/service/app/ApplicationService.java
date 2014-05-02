package com.siemens.cto.aem.service.app;

import java.util.List;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    List<Application> getApplications(PaginationParameter somePagination);

    List<Application> findApplications(Identifier<Group> groupId, PaginationParameter somePagination);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId, PaginationParameter somePagination);

}
