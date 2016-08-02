package com.siemens.cto.aem.service.balancermanager;

public interface BalancermanagerService {

    void drainUserGroup(final String groupName);

    void drainUserWebServer(final String groupName, final String webserverName);

    void getGroupDrainStatus(final String group);

}
