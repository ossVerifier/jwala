package com.siemens.cto.aem.persistence.dao.app;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

import java.util.List;

public interface ApplicationDao {

    Application getApplication(final Identifier<Application> aApplicationId)
            throws NotFoundException;

    List<Application> getApplications();

    List<Application> findApplications(final String aGroupName);
    List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId);

    List<Application> findApplicationsBelongingToWebServer(String aWebServerName);

}
