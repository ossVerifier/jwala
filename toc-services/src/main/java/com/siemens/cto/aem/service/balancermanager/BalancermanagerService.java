package com.siemens.cto.aem.service.balancermanager;

import org.springframework.http.HttpStatus;

/**
 * Created by LW044480 on 7/25/2016.
 */
public interface BalancermanagerService {

    HttpStatus drainUserGroup(final String groupName);

    void drainUserWebServer(final String groupName, final String webserverName);

}
