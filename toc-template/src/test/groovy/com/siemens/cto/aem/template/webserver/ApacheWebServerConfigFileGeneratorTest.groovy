package com.siemens.cto.aem.template.webserver

import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.group.LiteGroup
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.jvm.JvmState
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath
import com.siemens.cto.aem.common.domain.model.path.Path
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException

/**
 * Unit test for {@link ApacheWebServerConfigFileGenerator}
 *
 * Created by Z003BPEJ on 6/23/14.
 */
class ApacheWebServerConfigFileGeneratorTest extends GroovyTestCase {

    List<Jvm> jvms
    List<Application> apps
    WebServer webServer

    void setUp() {
        webServer = new WebServer(null, new HashSet<LiteGroup>(), "Apache2.4", "localhost", 80, 443,
                                  new Path("/statusPath"), new FileSystemPath("D:/stp/http-2.4.9/conf/httpd.conf"),
                                  new Path("./"), new Path("htdocs"))
        
        jvms = new ArrayList<>()
        jvms.add(new Jvm(null, "tc1", "165.226.8.129", new HashSet<LiteGroup>(), null, null, null, null, 8009,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, null))
        jvms.add(new Jvm(null, "t c 2", "165.22 6.8.129", new HashSet<LiteGroup>(), null, null, null, null, 8109,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, null))

        apps = new ArrayList<>()
        apps.add(new Application(null, "hello-world-1", null, "/hello-world-1", null, true, true, "testWar.war"))
        apps.add(new Application(null, "hello-world-2", null, "/hello-world-2", null, true, true, "testWar.war"))
        apps.add(new Application(null, "hello-world-3", null, "/hello-world-3", null, true, true, "testWar.war"))
    }

    void testGetHttpdConf() {
        final String refFileText = removeCarriageReturnsAndNewLines(this.getClass().getResource("/httpd.conf").text)
        assert refFileText == removeCarriageReturnsAndNewLines(
                ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-conf.tpl", webServer, jvms, apps))
    }

    void testGetHttpdConfWithSsl() {
        final String refFileText = removeCarriageReturnsAndNewLines(this.getClass().getResource("/httpd-ssl.conf").text)
        assert refFileText == removeCarriageReturnsAndNewLines(
                ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-ssl-conf.tpl", webServer, jvms, apps))
    }

    void testGetHttpdConfMissingTemplate() {
        shouldFail(TemplateNotFoundException) {
            ApacheWebServerConfigFileGenerator.getHttpdConf("Apache2.4", "/httpd-conf-fictitious.tpl", webServer, jvms, apps)
        }
    }

    void testGetWorkersProperties() {
        final String refFileText =
                removeCarriageReturnsAndNewLines(this.getClass().getResource("/workers.properties").text)
        assert refFileText.equalsIgnoreCase(
                removeCarriageReturnsAndNewLines(
                    ApacheWebServerConfigFileGenerator
                            .getWorkersProperties("Apache2.4", "/workers-properties.tpl", jvms, apps)
                                .replaceAll("(?m)^[ \\t]*\\r?\\n","")))
    }

    void testGetWorkerPropertiesMissingTemplate() {
        shouldFail(TemplateNotFoundException) {
            ApacheWebServerConfigFileGenerator
                    .getWorkersProperties("Apache2.4", "/workers-properties-fictitious.tpl", jvms, apps)
        }
    }

    private String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "")
    }

}