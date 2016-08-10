package com.cerner.jwala.service.balancermanager;

import com.cerner.jwala.common.domain.model.balancermanager.DrainStatus;

public interface BalancermanagerService {

    DrainStatus drainUserGroup(final String groupName, final String webServersNameMap);

    DrainStatus drainUserWebServer(final String groupName, final String webServerName);

    DrainStatus getGroupDrainStatus(final String group);

}
