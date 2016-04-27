package com.siemens.cto.aem.template

import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import groovy.text.GStringTemplateEngine

class ResourceFileGenerator {
    static <T> String generateResourceConfig(String templateText, ResourceGroup resourceGroup, T selectedValue) {
        WebServer webServer = null;
        Jvm jvm = null;
        Application webApp = null;
        if(selectedValue.class == WebServer.class) {
            webServer = (WebServer)selectedValue;
        } else if(selectedValue.class == Jvm.class) {
            jvm = (Jvm)selectedValue;
        } else if(selectedValue.class == webApp.class) {
            webApp = (Application)selectedValue;
        }
        final binding = [webServers     : resourceGroup.getWebServers(),
                         webServer      : webServer,
                         jvms           : resourceGroup.getJvms(),
                         jvm            : jvm,
                         webApps        : resourceGroup.getApplications(),
                         webApp         : webApp]

        final engine = new GStringTemplateEngine()

        return engine.createTemplate(templateText).make(binding.withDefault{''})
    }
}
