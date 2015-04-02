package com.siemens.cto.aem.service.resource.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.TemplateManager;
import org.springframework.transaction.annotation.Transactional;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final TemplateManager templateManager;
    private final HarmonyTemplateEngine templateEngine;
    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;

    private final String encryptExpressionString="new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )"; 
    
    public ResourceServiceImpl(
            final TemplateManager theTemplateManager,
            final HarmonyTemplateEngine harmonyTemplateEngine,
            final ResourcePersistenceService resourcePersistenceService,
            final GroupPersistenceService groupPersistenceService
            ) {
        templateManager = theTemplateManager;
        templateEngine = harmonyTemplateEngine;
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        expressionParser = new SpelExpressionParser();
        encryptExpression = expressionParser.parseExpression(encryptExpressionString);
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            Collection<ResourceType> resourceTypes = templateManager.getResourceTypes();
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
    public ResourceInstance getResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) {
        return this.resourcePersistenceService.getResourceInstance(aResourceInstanceId);
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupName(String groupName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupId(group.getId().getId());
    }

    @Override
    public ResourceInstance getResourceInstanceByGroupNameAndName(String groupName, String name) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstanceByGroupIdAndName(group.getId().getId(), name);
    }

    @Override
    public List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(String groupName, String resourceTypeName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupIdAndResourceTypeName(group.getId().getId(), resourceTypeName);
    }

    @Override
    @Transactional
    public ResourceInstance createResourceInstance(CreateResourceInstanceCommand createResourceInstanceCommand, User creatingUser) {
        return this.resourcePersistenceService.createResourceInstance(new Event<CreateResourceInstanceCommand>(createResourceInstanceCommand, AuditEvent.now(creatingUser)));
    }

    @Override
    public ResourceInstance updateResourceInstanceAttributes(UpdateResourceInstanceAttributesCommand updateResourceInstanceAttributesCommand, User updatingUser) {
        return this.resourcePersistenceService.updateResourceInstanceAttributes(new Event<UpdateResourceInstanceAttributesCommand>(updateResourceInstanceAttributesCommand, AuditEvent.now(updatingUser)));
    }

    @Override
    public ResourceInstance updateResourceInstanceFriendlyName(UpdateResourceInstanceNameCommand updateResourceInstanceFriendlyNameCommand, User updatingUser) {
        return this.resourcePersistenceService.updateResourceInstanceFriendlyName(new Event<UpdateResourceInstanceNameCommand>(updateResourceInstanceFriendlyNameCommand, AuditEvent.now(updatingUser)));
    }

    @Override
    public void deleteResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) {
        this.resourcePersistenceService.deleteResourceInstance(aResourceInstanceId);
    }

    @Override
    public String encryptUsingPlatformBean(String cleartext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);        
        String result = encryptExpression.getValue(context, String.class);
        return result;
    }
}
