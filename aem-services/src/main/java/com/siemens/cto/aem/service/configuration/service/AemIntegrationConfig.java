package com.siemens.cto.aem.service.configuration.service;

import java.util.concurrent.ThreadFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.siemens.cto.aem.service.configuration.lifecycle.HeartbeatStartupLifecycleListener;

@Configuration
@ImportResource({
    "classpath:META-INF/spring/integration.xml",
    "classpath:META-INF/spring/integration-state.xml",
    "classpath:META-INF/spring/integration-jmx.xml",
    "classpath:META-INF/spring/jvm-heartbeat-integration.xml",
    "classpath:META-INF/spring/common-heartbeat-dependencies.xml"
})
public class AemIntegrationConfig {

    @Bean
    public HeartbeatStartupLifecycleListener getHeartbeatStartupLifecycleListener() {
        return new HeartbeatStartupLifecycleListener();
    }
    
    /**
     * Bean method to create a thread factory that creates daemon threads.
     * <code>
    <bean id="pollingThreadFactory" class="org.springframework.scheduling.concurrent.CustomizableThreadFactory">
        <constructor-arg value="polling-"/>
    </bean></code> */
    @Bean(name="pollingThreadFactory") public ThreadFactory getPollingThreadFactory() {
        CustomizableThreadFactory tf = new CustomizableThreadFactory("polling-");
        tf.setDaemon(true);
        return tf;
    }

}
