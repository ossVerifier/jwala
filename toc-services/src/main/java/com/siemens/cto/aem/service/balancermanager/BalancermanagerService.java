package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;

public interface BalancermanagerService {

    void drainUserGroup(final String groupName);

    void drainUserWebServer(final String groupName, final String webserverName);

    DrainStatus getGroupDrainStatus(final String group);

}
