package com.siemens.cto.aem.template

import com.siemens.cto.aem.domain.model.group.Group
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException
import com.siemens.cto.toc.files.FileManager
import groovy.text.GStringTemplateEngine
import java.nio.file.Path

public class HarmonyTemplateEngine {

    FileManager templateManager;
    def engine = new GStringTemplateEngine();

    public HarmonyTemplateEngine(FileManager theTemplateManager) {
        this.templateManager = theTemplateManager;
    }
    
    private def createDummyObjectModel() { return [ allPropsXml: ""]; }
    // TBD private def createHarmonyObjectModel() { return [ allPropsXml: ""]; }
    
    public HarmonyTemplate getTemplate(ResourceType rtype) { 
        return createHarmonyTemplateFromResourceType(this, rtype);
    }
    
    def createHarmonyTemplateFromResourceType(engine, ResourceType resourceType) {
        return new HarmonyTemplate(
            resourceType,
            templateManager.getTemplatePathForResourceType(resourceType),
            engine);
    }

    def checkOnly(Path templatePath) { 
        
        def binding = createDummyObjectModel();
        def templateFileName = templatePath.toAbsolutePath().toString();         
        def resource = templatePath.toFile()

        if(!resource.exists()) {
            resource = getClass().getResource(templateFileName)
        }         
        
        if(resource == null) { 
            throw new TemplateNotFoundException(templateFileName, null)
        }
        
        String text = resource.getText();
        
        return engine.createTemplate(text)
    }
    public String populateMasterTemplate(String masterTempateName, Group group, Map<String, String> additionalBindings) {

    }

    public String populateResourceInstanceTemplate(ResourceInstance resourceInstance, Map<String, String> additionalBindings, Map<String, String> mockedValues) {
        String template = templateManager.getResourceTypeTemplate(resourceInstance.getResourceTypeName());

        Map<String, String> resouceInstanceAttributes = resourceInstance.getAttributes();
        if (additionalBindings != null) {
            resouceInstanceAttributes.putAll(additionalBindings);
        }
        if (mockedValues != null && !mockedValues.isEmpty()) {
            for (String key: mockedValues.keySet()) {
                if (template.contains(key)) {
                    resouceInstanceAttributes.put(key, mockedValues.get(key));
                }
            }
        }
        return engine.createTemplate(template).make(resouceInstanceAttributes);
    }

    public String getTemplate(final String resourceTypeName) {
        return templateManager.getResourceTypeTemplate(resourceTypeName);
    }
}
