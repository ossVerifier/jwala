package com.siemens.cto.aem.template
import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import groovy.text.GStringTemplateEngine

class ResourceFileGenerator {
    static String generateResourceConfig(String templateText, List<WebServer> webServers, WebServer webServer, List<Jvm> jvms, Jvm jvm, List<Application> applications, Application application) {
        final binding = [webServers     : webServers,
                         webServer      : webServer,
                         jvms           : jvms,
                         jvm            : jvm,
                         webApps        : applications,
                         webApp         : application]

        final engine = new GStringTemplateEngine()

        return engine.createTemplate(templateText).make(binding.withDefault{''})
    }
}
