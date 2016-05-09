package com.siemens.cto.aem.template

import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.group.Group
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import groovy.text.GStringTemplateEngine

class ResourceFileGenerator {
    static <T> String generateResourceConfig(String templateText, ResourceGroup resourceGroup, T selectedValue) {
        Group group = null
        WebServer webServer = null
        Jvm jvm = null
        Application webApp = null

        List<Group> groups = resourceGroup.getGroups();
        List<WebServer> webServers = null;
        List<Application> webApps = null;
        List<Jvm> jvms = null;

        if(selectedValue.class == WebServer.class) {
            webServer = (WebServer)selectedValue
            group = webServer.getParentGroup()
        } else if(selectedValue.class == Jvm.class) {
            jvm = (Jvm)selectedValue
            group = jvm.getParentGroup()
        } else if(selectedValue.class == webApp.class) {
            webApp = (Application)selectedValue
            jvm = webApp.getParentJvm()
            group = webApp.getGroup()
        }
        groups.each {
            if(webServers == null) {
                webServers = new ArrayList<>();
            }
            if(webApps == null) {
                webApps = new ArrayList<>();
            }
            if(jvms == null) {
                jvms = new ArrayList<>();
            }
            webServers.addAll(it.getWebServers());
            webApps.addAll(it.getApplications());
            jvms.addAll(it.getJvms());
        }
        final binding = [webServers     : webServers,
                         webServer      : webServer,
                         jvms           : jvms,
                         jvm            : jvm,
                         webApps        : webApps,
                         webApp         : webApp,
                         groups         : groups,
                         group          : group ]

        final engine = new GStringTemplateEngine()

        return engine.createTemplate(templateText).make(binding.withDefault{''})
    }
}
