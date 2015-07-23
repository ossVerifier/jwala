package com.siemens.cto.aem.template.configuration

import com.siemens.cto.aem.template.HarmonyTemplateEngine
import com.siemens.cto.toc.files.FileManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TocTemplateConfiguration {

    @Autowired
    FileManager templateManager;
    
    @Bean
    def HarmonyTemplateEngine getHarmonyTemplateEngine() {
        return new HarmonyTemplateEngine(templateManager);
    }
}
