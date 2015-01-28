package com.siemens.cto.aem.template;

import com.siemens.cto.aem.domain.model.resource.ResourceType
import com.siemens.cto.aem.template.webserver.exception.TemplateNotFoundException
import com.siemens.cto.toc.files.TemplateManager
import groovy.text.GStringTemplateEngine
import java.nio.file.Path

public class HarmonyTemplateEngine {

    TemplateManager templateManager;
    def engine = new GStringTemplateEngine();
    
    public HarmonyTemplateEngine(TemplateManager theTemplateManager) {
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
    
}
