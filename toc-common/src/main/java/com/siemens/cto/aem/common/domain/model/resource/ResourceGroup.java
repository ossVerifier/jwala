package com.siemens.cto.aem.common.domain.model.resource;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SP043299 on 4/25/2016.
 *
 * This class contains all the information of a group resource. It contains the list of webservers, jvms and webapps for a selected group.
 */
public class ResourceGroup {
    private List<Group> groups;

    public ResourceGroup() {
    }

    public ResourceGroup(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "ResourceGroup{" +
                "groups=" + groups +
                '}';
    }
}
