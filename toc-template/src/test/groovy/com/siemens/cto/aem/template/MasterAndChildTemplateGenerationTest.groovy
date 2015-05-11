package com.siemens.cto.aem.template

import com.siemens.cto.aem.domain.model.group.Group
import com.siemens.cto.aem.domain.model.group.LiteGroup
import com.siemens.cto.aem.domain.model.id.Identifier
import com.siemens.cto.aem.domain.model.resource.ResourceInstance
import groovy.text.SimpleTemplateEngine;

/**
 * Test generation of a child template in which its result is used in the generation of a master template using
 * Groovy's SimpleTemplateEngine.
 *
 * Created by Z003BPEJ on 5/1/2015.
 */
class MasterAndChildTemplateGenerationTest extends GroovyTestCase {
    private String contextTemplateStr
    private String expectedContextXmlStr

    private String iteritiveContextTemplateStr
    private String expectedIteritiveContextXmlStr

    private final List<ResourceInstance> resourceInstanceList = new ArrayList<>()
    final SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    /**
     * The wrapper class as seen in Peter Horsfield's example.
     */
    class ResourceInstanceTemplateBinding {
        final ResourceInstance resourceInstance;
        def resourceTemplate

        def ResourceInstanceTemplateBinding(final ResourceInstance resourceInstance) {
            this.resourceInstance = resourceInstance
            resourceTemplate = templateEngine.createTemplate(this.getClass().getResource("/" +
                                            resourceInstance.getResourceTypeName().replaceAll(" ", "") + ".tpl").text)
        }

        @Override
        public String toString() {
            return resourceTemplate.make(resourceInstance.getAttributes()).toString()
        }
    }

    void setUp() {
        contextTemplateStr = this.getClass().getResource("/context.tpl").text
        expectedContextXmlStr = this.getClass().getResource("/context.xml").text

        iteritiveContextTemplateStr = this.getClass().getResource("/iterativeContext.tpl").text
        expectedIteritiveContextXmlStr = this.getClass().getResource("/iterativeContext.xml").text

        final LiteGroup liteGroup = new LiteGroup(new Identifier<Group>("1"), "Test Group")

        // Resource 1
        final Map<String, String> jmsTopicAttributes = new LinkedHashMap<>()
        jmsTopicAttributes.put("name", "someTopicName")
        jmsTopicAttributes.put("auth", "someTopicAuth")
        jmsTopicAttributes.put("description", "someTopicDescription")
        jmsTopicAttributes.put("factory", "someTopicFactory")
        jmsTopicAttributes.put("physicalName", "someTopicPhysicalName")
        jmsTopicAttributes.put("type", "someTopicType")

        // Setup resource instances
        final ResourceInstance jmsTopicResourceInstance = new ResourceInstance(new Identifier<ResourceInstance>("1"),
                                                                               "Test JMS Topic",
                                                                               "JMS Topic",
                                                                               liteGroup,
                                                                               jmsTopicAttributes)

        resourceInstanceList.add(jmsTopicResourceInstance)

        // Resource 2
        final Map<String, String> jmsQueueAttributes = new LinkedHashMap<>()
        jmsQueueAttributes.put("name", "someQueueName")
        jmsQueueAttributes.put("auth", "someQueueAuth")
        jmsQueueAttributes.put("description", "someQueueDescription")
        jmsQueueAttributes.put("factory", "someQueueFactory")
        jmsQueueAttributes.put("physicalName", "someQueuePhysicalName")
        jmsQueueAttributes.put("type", "someQueueType")

        // Setup resource instances
        final ResourceInstance jmsQueueResourceInstance = new ResourceInstance(new Identifier<ResourceInstance>("2"),
                                                                               "Test JMS Queue",
                                                                               "JMS Queue",
                                                                               liteGroup,
                                                                               jmsQueueAttributes)

        resourceInstanceList.add(jmsQueueResourceInstance)
    }

    void testMasterAndChildTemplateGeneration() {
        final Map<String, String> resourceMap = new LinkedHashMap<>()
        for (ResourceInstance resourceInstance : resourceInstanceList) {
            resourceMap.put(resourceInstance.getResourceTypeName(), (new ResourceInstanceTemplateBinding(resourceInstance)).toString())
        }

        def contextTemplate = templateEngine.createTemplate(contextTemplateStr)

        def contextBindings = [docBase:"/some-doc-base", resourceMap:resourceMap]

        // Note: indexOfLastXmlClosingBracket is used to make sure that any additional characters e.g. line feed
        //       after the closing XML bracket are not included in the XML string comparison.
        int indexOfLastXmlClosingBracket = expectedContextXmlStr.lastIndexOf('>')
        assertEquals(expectedContextXmlStr.substring(0, indexOfLastXmlClosingBracket + 1),
                contextTemplate.make(contextBindings).toString().substring(0, indexOfLastXmlClosingBracket + 1))

        contextTemplate = templateEngine.createTemplate(iteritiveContextTemplateStr)
        indexOfLastXmlClosingBracket = expectedIteritiveContextXmlStr.lastIndexOf('>')
        assertEquals(expectedIteritiveContextXmlStr.substring(0, indexOfLastXmlClosingBracket + 1),
                contextTemplate.make(contextBindings).toString().substring(0, indexOfLastXmlClosingBracket + 1))
    }
}