package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;

public interface BalancermanagerService {

    DrainStatus drainUserGroup(final String groupName, final String webServersNameMap);

    DrainStatus drainUserWebServer(final String groupName, final String webServerName);

    DrainStatus getGroupDrainStatus(final String group);

}
