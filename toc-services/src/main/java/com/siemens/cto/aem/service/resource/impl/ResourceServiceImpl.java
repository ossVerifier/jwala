package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final FileManager fileManager;
    private final HarmonyTemplateEngine templateEngine;
    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;

    private final String encryptExpressionString="new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )"; 
    
    public ResourceServiceImpl(
            final FileManager theFileManager,
            final HarmonyTemplateEngine harmonyTemplateEngine,
            final ResourcePersistenceService resourcePersistenceService,
            final GroupPersistenceService groupPersistenceService
            ) {
        fileManager = theFileManager;
        templateEngine = harmonyTemplateEngine;
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        expressionParser = new SpelExpressionParser();
        encryptExpression = expressionParser.parseExpression(encryptExpressionString);
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            Collection<ResourceType> resourceTypes = fileManager.getResourceTypes();
            for(ResourceType rtype : resourceTypes) {
                if(rtype.isValid()) {
                    HarmonyTemplate template = templateEngine.getTemplate(rtype);
                    try {
                        template.check();
                    } catch(Exception exception) { 
                        LOGGER.debug("During getResourceTypes, discovered a bad template", exception);
                        rtype.setValid(false);
                        rtype.addException(exception);
                    }
                }
            }
            return resourceTypes;
        } catch (IOException e) {
            // This is extremely unlikely since we return ResourceTypes(valid=false) even when files are invalid. 
            String errorString = "Failed to get resource types from disk.";
            LOGGER.error(errorString, e);
            throw new FaultCodeException(AemFaultType.INVALID_PATH, errorString, e);
        }
    }

    @Override
    public ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) {
        return this.resourcePersistenceService.getResourceInstance(aResourceInstanceId);
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupName(final String groupName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupId(group.getId().getId());
    }

    @Override
    public ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstanceByGroupIdAndName(group.getId().getId(), name);
    }

    @Override
    public String generateResourceInstanceFragment(String groupName, String resourceInstanceName) {
        final Map<String, String> mockedValues = new HashMap<>();
        mockedValues.put("jvm.id", "[jvm.id of instance]");
        mockedValues.put("jvm.name", "[jvm.name of instance]");
        mockedValues.put("app.name", "[app.name of Web App]");
        return generateResourceInstanceFragment(groupName, resourceInstanceName, mockedValues);
    }

    @Override
    public String generateResourceInstanceFragment(String groupName, String resourceInstanceName, Map<String, String> mockedValues) {
        ResourceInstance resourceInstance =  this.getResourceInstanceByGroupNameAndName(groupName, resourceInstanceName);
        return templateEngine.populateResourceInstanceTemplate(resourceInstance, null, mockedValues);
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupIdAndResourceTypeName(group.getId().getId(), resourceTypeName);
    }

    @Override
    @Transactional
    public ResourceInstance createResourceInstance(final ResourceInstanceRequest createResourceInstanceCommand, final User creatingUser) {
        this.groupPersistenceService.getGroup(createResourceInstanceCommand.getGroupName());
        return this.resourcePersistenceService.createResourceInstance(new Event<ResourceInstanceRequest>(createResourceInstanceCommand, AuditEvent.now(creatingUser)));
    }

    @Override
    @Transactional
    public ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceRequest updateResourceInstanceCommand, final User updatingUser) {
        ResourceInstance resourceInstance = this.getResourceInstanceByGroupNameAndName(groupName, name);
        return this.resourcePersistenceService.updateResourceInstance(resourceInstance, new Event<ResourceInstanceRequest>(updateResourceInstanceCommand, AuditEvent.now(updatingUser)));
    }

    @Override
    public void deleteResourceInstance(final String groupName, final String name) {
        ResourceInstance resourceInstance = this.getResourceInstanceByGroupNameAndName(groupName, name);
        this.resourcePersistenceService.deleteResourceInstance(resourceInstance.getResourceInstanceId());
    }

    @Override
    @Transactional
    public void deleteResources(final String groupName, final List<String> resourceNames) {
        resourcePersistenceService.deleteResources(groupName, resourceNames);
    }

    @Override
    public String encryptUsingPlatformBean(String cleartext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);        
        String result = encryptExpression.getValue(context, String.class);
        return result;
    }

    @Override
    public String getTemplate(final String resourceTypeName) {
        return templateEngine.getTemplate(resourceTypeName);
    }
}
