package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.balancermanager.BalancerManagerState;
import com.siemens.cto.aem.common.domain.model.user.User;

public interface BalancermanagerService {

    BalancerManagerState drainUserGroup(final String groupName, final String webServersNameMap, final User user);

    BalancerManagerState drainUserWebServer(final String groupName, final String webServerName, final User user);

    BalancerManagerState getGroupDrainStatus(final String group, final User user);

}
