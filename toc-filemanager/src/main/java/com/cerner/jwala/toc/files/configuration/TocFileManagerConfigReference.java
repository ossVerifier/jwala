package com.cerner.jwala.toc.files.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({TocFileManagerConfiguration.class})
public class TocFileManagerConfigReference {

}
