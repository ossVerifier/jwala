package com.siemens.cto.aem.persistence.dao.app;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

import java.util.List;

public interface ApplicationDao {

    Application getApplication(final Identifier<Application> aApplicationId)
            throws NotFoundException;

    List<Application> getApplications();

    List<Application> findApplications(final String aGroupName);
    List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId);

    List<Application> findApplicationsBelongingToWebServer(String aWebServerName);

    Application findApplicationByName(final String name);

    Application findApplication(String appName, String groupName, String jvmName);

}
