package com.cerner.jwala.service.balancermanager;


import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;

public interface BalancerManagerService {

    BalancerManagerState drainUserGroup(final String groupName, final String webServersNameMap, final String user);

    BalancerManagerState drainUserWebServer(final String groupName, final String webServerName, final String user);

    BalancerManagerState drainUserJvm(final String groupName, final String hostName, final String jvmName, final String user);

    BalancerManagerState getGroupDrainStatus(final String group, final String user);

}