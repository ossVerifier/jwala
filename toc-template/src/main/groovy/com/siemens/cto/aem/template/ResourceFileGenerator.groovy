package com.siemens.cto.aem.template

import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.group.Group
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import com.siemens.cto.aem.common.properties.ApplicationProperties
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

        if (selectedValue instanceof WebServer) {
            webServer = selectedValue as WebServer
            group = webServer.getParentGroup()
        } else if (selectedValue instanceof Jvm) {
            jvm = selectedValue as Jvm
            group = jvm.getParentGroup()
        } else if (selectedValue instanceof Application) {
            webApp = selectedValue as Application
            jvm = webApp.getParentJvm()
            group = webApp.getGroup()
        }
        groups.each {
            if (it.getWebServers() != null) {
                if (webServers == null) {
                    webServers = new ArrayList<>();
                }
                webServers.addAll(it.getWebServers());
            }

            if (it.getApplications() != null) {
                if (webApps == null) {
                    webApps = new ArrayList<>();
                }
                webApps.addAll(it.getApplications());
            }

            if (it.getJvms() != null) {
                if (jvms == null) {
                    jvms = new ArrayList<>();
                }
                jvms.addAll(it.getJvms());
            }
        }
        final map = new HashMap<String, String>(ApplicationProperties.properties);
        def binding = [webServers   : webServers,
                       webServer    : webServer,
                       jvms         : jvms,
                       jvm          : jvm,
                       webApps      : webApps,
                       webApp       : webApp,
                       groups       : groups,
                       group        : group,
                       varsProperties: map]

        final engine = new GStringTemplateEngine()

        return engine.createTemplate(templateText).make(binding.withDefault { '' })
    }
}
