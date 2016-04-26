package com.siemens.cto.aem.common.domain.model.resource;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SP043299 on 4/25/2016.
 */
public class ResourceGroup {
    private List<WebServer> webServers;
    private WebServer selectedWebServer;
    private List<Jvm> jvms;
    private Jvm selectedJvm;
    private List<Application> applications;
    private Application selectedApplication;

    public ResourceGroup(List<WebServer> webServers, WebServer selectedWebServer, List<Jvm> jvms, Jvm selectedJvm, List<Application> applications, Application selectedApplication) {
        this.webServers = webServers;
        this.selectedWebServer = selectedWebServer;
        this.jvms = jvms;
        this.selectedJvm = selectedJvm;
        this.applications = applications;
        this.selectedApplication = selectedApplication;
    }

    public List<WebServer> getWebServers() {
        return webServers;
    }

    public void setWebServers(List<WebServer> webServers) {
        this.webServers = webServers;
    }

    public WebServer getSelectedWebServer() {
        return selectedWebServer;
    }

    public void setSelectedWebServer(WebServer selectedWebServer) {
        this.selectedWebServer = selectedWebServer;
    }

    public List<Jvm> getJvms() {
        return jvms;
    }

    public void setJvms(List<Jvm> jvms) {
        this.jvms = jvms;
    }

    public Jvm getSelectedJvm() {
        return selectedJvm;
    }

    public void setSelectedJvm(Jvm selectedJvm) {
        this.selectedJvm = selectedJvm;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public Application getSelectedApplication() {
        return selectedApplication;
    }

    public void setSelectedApplication(Application selectedApplication) {
        this.selectedApplication = selectedApplication;
    }

    @Override
    public String toString() {
        return "ResourceGroup{" +
                "webServers=" + webServers +
                ", selectedWebServer=" + selectedWebServer +
                ", jvms=" + jvms +
                ", selectedJvm=" + selectedJvm +
                ", applications=" + applications +
                ", selectedApplication=" + selectedApplication +
                '}';
    }
}
