package com.siemens.cto.aem.template

import groovy.text.SimpleTemplateEngine;

/**
 * Test generation of a child template in which its result is used in the generation of a master template using
 * Groovy's SimpleTemplateEngine.
 *
 * Created by Z003BPEJ on 5/1/2015.
 */
class MasterAndChildTemplateGenerationTest extends GroovyTestCase {
    private String contextTemplateStr;
    private String jmsTopicTemplateStr;
    private String contextXmlStr;

    void setUp() {
        contextTemplateStr = this.getClass().getResource("/context.tpl").text
        jmsTopicTemplateStr = this.getClass().getResource("/jms-topic.tpl").text
        contextXmlStr = this.getClass().getResource("/context.xml").text
    }

    void testMasterAndChildTemplateGeneration() {
        final SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

        def jmsTopicTemplate = templateEngine.createTemplate(jmsTopicTemplateStr)
        def jmsTopicBindings = [name:"someTopicName",
                                auth:"someAuth",
                                description:"someDescription",
                                factory:"someFactory",
                                physicalName:"somePhysicalName",
                                type:"someType"];
        def jmsTopicBindingsResult = jmsTopicTemplate.make(jmsTopicBindings).toString();

        def contextTemplate = templateEngine.createTemplate(contextTemplateStr)
        def contextBindings = [docBase:"/some-doc-base", jmsTopicResource:jmsTopicBindingsResult];

        assertEquals(contextXmlStr, contextTemplate.make(contextBindings).toString())
    }
}