package com.siemens.cto.aem.template
import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import groovy.text.GStringTemplateEngine

class ResourceFileGenerator {
    static String generateResourceConfig(String templateText, ResourceGroup resourceGroup) {
        final binding = [webServers     : resourceGroup.getWebServers(),
                         webServer      : resourceGroup.getSelectedWebServer(),
                         jvms           : resourceGroup.getJvms(),
                         jvm            : resourceGroup.getSelectedJvm(),
                         webApps        : resourceGroup.getApplications(),
                         webApp         : resourceGroup.getSelectedApplication()]

        final engine = new GStringTemplateEngine()

        return engine.createTemplate(templateText).make(binding.withDefault{''})
    }
}
