package com.siemens.cto.aem.template.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import([
    TocTemplateConfiguration.class
])
class TocTemplateConfigurationReference { }
