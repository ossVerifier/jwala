package com.siemens.cto.aem.common.domain.model.resource;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import java.util.List;

/**
 * Created by SP043299 on 4/25/2016.
 *
 * This class contains all the information of a group resource.
 */
public class ResourceGroup {
    private List<WebServer> webServers;
    private List<Jvm> jvms;
    private List<Application> applications;

    public ResourceGroup() {
    }

    public ResourceGroup(List<WebServer> webServers, List<Jvm> jvms, List<Application> applications) {
        this.webServers = webServers;
        this.jvms = jvms;
        this.applications = applications;
    }

    public List<WebServer> getWebServers() {
        return webServers;
    }

    public void setWebServers(List<WebServer> webServers) {
        this.webServers = webServers;
    }

    public List<Jvm> getJvms() {
        return jvms;
    }

    public void setJvms(List<Jvm> jvms) {
        this.jvms = jvms;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    @Override
    public String toString() {
        return "ResourceGroup{" +
                "webServers=" + webServers +
                ", jvms=" + jvms +
                ", applications=" + applications +
                '}';
    }
}
