package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.command.resource.ResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface ResourceService {

    Collection<ResourceType> getResourceTypes();

    ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId);

    List<ResourceInstance> getResourceInstancesByGroupName(final String groupName);

    ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name);

    String generateResourceInstanceFragment(final String groupName, final String name);

    String generateResourceInstanceFragment(String groupName, String resourceInstanceName, Map<String, String> mockedValues);

    List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName);

    ResourceInstance createResourceInstance(final ResourceInstanceCommand createResourceInstanceCommand, final User creatingUser);

    ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceCommand updateResourceInstanceAttributesCommand, final User updatingUser);

    void deleteResourceInstance(final String name, final String groupName);

    void deleteResources(final String groupName, final List<String> resourceNames);
    
    String  encryptUsingPlatformBean(String cleartext);

    String getTemplate(final String resourceTypeName);

}
