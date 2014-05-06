package com.siemens.cto.aem.service.app.impl;

import java.util.List;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.service.app.ApplicationService;

public class ApplicationServiceImpl implements ApplicationService {

    private ApplicationDao applicationDao;

    public ApplicationServiceImpl(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationDao.getApplication(aApplicationId);
    }

    @Override
    public List<Application> getApplications(PaginationParameter somePagination) {
        return applicationDao.getApplications(somePagination);
    }

    @Override
    public List<Application> findApplications(Identifier<Group> groupId, PaginationParameter somePagination) {
        return applicationDao.findApplicationsBelongingTo(groupId, somePagination);
    }

    @Override
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId, PaginationParameter somePagination) {
        return applicationDao.findApplicationsBelongingToJvm(jvmId, somePagination);
    }

    @Override
    public Application updateApplication(UpdateApplicationCommand anAppToUpdate, User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application createApplication(CreateApplicationCommand anAppToCreate, User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        // TODO Auto-generated method stub
    }
}
