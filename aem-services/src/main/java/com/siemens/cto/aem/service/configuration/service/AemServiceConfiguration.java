package com.siemens.cto.aem.service.configuration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;

@Configuration
public class AemServiceConfiguration {

    @Autowired
    private AemDaoConfiguration daoConfiguration;

    @Bean
    public GroupService getGroupService() {
        return new GroupServiceImpl(daoConfiguration.getGroupDao(),
                                    getJvmService());
    }

    @Bean
    public JvmService getJvmService() {
        return new JvmServiceImpl(daoConfiguration.getJvmDao());
    }

}
