package com.siemens.cto.aem.service.webserver

import com.siemens.cto.aem.service.webserver.exception.HttpdConfigTemplateNotFoundException

/**
 * Unit test for {@link HttpdConfigGenerator}
 *
 * Created by Z003BPEJ on 6/23/14.
 */
class HttpdConfigGeneratorTest extends GroovyTestCase {

    def binding
    def result

    void setUp() {
        binding = [
            app:[
                    [
                        name  : "hello-world-1",
                        mount : "/hello-world-1/*"
                    ],
                    [
                        name  : "hello-world-2",
                        mount : "/hello-world-2/*"
                    ]
                ]
        ]

        result = this.getClass().getResource("/httpd.conf").text.replaceAll("\\s+","")
    }

    void testGetHttpdConf() {
        assert result == HttpdConfigGenerator.getHttpdConf("/httpd-conf.tpl", binding).replaceAll("\\s+","")
    }

    void testGetHttpdConfMissingTemplate() {
        shouldFail(HttpdConfigTemplateNotFoundException) {
            HttpdConfigGenerator.getHttpdConf("/httpd-conf-fictitious.tpl", binding)
        }
    }

}