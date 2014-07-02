package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.domain.model.app.Application
import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException

/**
 * Unit test for {@link ApacheWebServerConfigFileGenerator}
 *
 * Created by Z003BPEJ on 6/23/14.
 */
class ApacheWebServerConfigFileGeneratorTest extends GroovyTestCase {

    def List<Application> apps

    void setUp() {
        apps = new ArrayList<>()
        apps.add(new Application(null, "hello-world-1", null, "/hello-world-1", null))
        apps.add(new Application(null, "hello-world-2", null, "/hello-world-2", null))
    }

    void testGetHttpdConf() {
        def refFile = this.getClass().getResource("/httpd.conf").text.replaceAll("\\s+","")
        assert refFile == ApacheWebServerConfigFileGenerator.getHttpdConf("/httpd-conf.tpl", apps).replaceAll("\\s+","")
    }

    void testGetHttpdConfWithSsl() {
        def refFile = this.getClass().getResource("/httpd-ssl.conf").text.replaceAll("\\s+","")
        assert refFile == ApacheWebServerConfigFileGenerator.getHttpdConf("/httpd-ssl-conf.tpl", apps).replaceAll("\\s+","")
    }

    void testGetHttpdConfMissingTemplate() {
        shouldFail(HttpdConfigTemplateNotFoundException) {
            ApacheWebServerConfigFileGenerator.getHttpdConf("/httpd-conf-fictitious.tpl", apps)
        }
    }

}