package com.cerner.jwala.files.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({JwalaFileManagerConfiguration.class})
public class JwalaFileManagerConfigReference {

}
