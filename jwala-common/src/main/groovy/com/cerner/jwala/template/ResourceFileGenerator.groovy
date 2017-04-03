package com.cerner.jwala.template

import com.cerner.jwala.common.domain.model.app.Application
import com.cerner.jwala.common.domain.model.group.Group
import com.cerner.jwala.common.domain.model.jvm.Jvm
import com.cerner.jwala.common.domain.model.resource.ResourceGroup
import com.cerner.jwala.common.domain.model.webserver.WebServer
import com.cerner.jwala.common.properties.ApplicationProperties
import com.cerner.jwala.common.properties.ExternalProperties
import com.cerner.jwala.template.exception.ResourceFileGeneratorException
import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;

class ResourceFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFileGenerator.class);
    private static HashMap<String,TemplateCacheEntry> templates = new ConcurrentHashMap<String,TemplateCacheEntry>();
    private static Object locker = new Object();
	
	// 1 hour in millis = 60 minutes = 60 * 60 seconds = 60 * 60 * 1000 milliseconds
	private static long retentionTimeMillis = 3600000;
	
	class TemplateCacheEntry {
		public Template tmpl;
		public Long createTimeMillis;
	}
	
    static <T> String generateResourceConfig(String fileName, String templateText, ResourceGroup resourceGroup, T selectedValue) {
        Group group = null
        WebServer webServer = null
        Jvm jvm = null
        Application webApp = null
        String entityInfo

        List<Group> groups = resourceGroup.getGroups();
        List<WebServer> webServers = null;
        List<Application> webApps = null;
        List<Jvm> jvms = null;

        if (selectedValue instanceof WebServer) {
            webServer = selectedValue as WebServer
            group = webServer.getParentGroup()
            entityInfo = "WebServer: " + webServer.getName()
        } else if (selectedValue instanceof Jvm) {
            jvm = selectedValue as Jvm
            group = jvm.getParentGroup()
            entityInfo = "Jvm: " + jvm.getJvmName()
        } else if (selectedValue instanceof Application) {
            webApp = selectedValue as Application
            jvm = webApp.getParentJvm()
            group = webApp.getGroup()
            entityInfo = "WebApp:" + webApp.getName()
        }
        groups.each {
            if (it.getWebServers() != null) {
                if (webServers == null) {
                    webServers = new ArrayList<>();
                }
                webServers.addAll(it.getWebServers());
            }

            if (it.getApplications() != null) {
                if (webApps == null) {
                    webApps = new ArrayList<>();
                }
                webApps.addAll(it.getApplications());
            }

            if (it.getJvms() != null) {
                if (jvms == null) {
                    jvms = new ArrayList<>();
                }
                jvms.addAll(it.getJvms());
            }
        }
        final map = new HashMap<String, String>(ApplicationProperties.properties);

        def binding = [webServers: webServers,
                       webServer : webServer,
                       jvms      : jvms,
                       jvm       : jvm,
                       webApps   : webApps,
                       webApp    : webApp,
                       groups    : groups,
                       group     : group,
                       vars      : map];
        map.each { k, v -> println "${k}:${v}" }
        def properties = ExternalProperties.properties
        if (properties.size() > 0) {
            final extMap = new HashMap<String, String>(properties);
            binding.ext = extMap;
        }

        final engine = new StreamingTemplateEngine();
        TemplateCacheEntry templce = null;
        synchronized (locker) {
	        templce = templates.get(templateText);
	        if (templce==null) {
	        	templce = new TemplateCacheEntry();
	        	templce.templ = engine.createTemplate(templateText);
				templce.createTimeMillis = System.currentTimeMillis();
		        templates.put(templateText, templce);
	        	LOGGER.info("Created new template for:" + fileName);
	        } else {
	        	LOGGER.info("Re-using template for:" + fileName);
	        }
	        
		    for (Iterator<Map.Entry<String, TemplateCacheEntry>> it = templates.entrySet().iterator(); it.hasNext(); ) {
		      Map.Entry<String, TemplateCacheEntry> entry = it.next();
		      if (entry.createTimeMillis<(System.currentTimeMillis-retentionTimeMillis)) {
		        it.remove();
		      }
		    }
        }

        try {
            return templce.templ.make(binding.withDefault { '' })
        } catch (final Exception e) {
            final String messageHistory = "Failed to bind data and properties to : " + fileName.trim();
            final String message = messageHistory + " for " + entityInfo + ". Cause(s) of the failure is/are: " + e.getMessage()
            LOGGER.error(message, e)
            throw new ResourceFileGeneratorException(message, e)
        }
    }
}
