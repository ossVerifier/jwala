package com.siemens.cto.aem.service.resource;

import java.util.Collection;
import java.util.List;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.domain.model.temporary.User;


public interface ResourceService {

    Collection<ResourceType> getResourceTypes();

    ResourceInstance getResourceInstance(Identifier<ResourceInstance> aResourceInstanceId);

    List<ResourceInstance> getResourceInstancesByGroupName(String groupName);

    ResourceInstance getResourceInstanceByGroupNameAndName(String groupName, String name);

    List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(String groupName, String resourceTypeName);

    ResourceInstance createResourceInstance(CreateResourceInstanceCommand createResourceInstanceCommand, User creatingUser);

    ResourceInstance updateResourceInstanceAttributes(UpdateResourceInstanceAttributesCommand updateResourceInstanceAttributesCommand, User updatingUser);

    ResourceInstance updateResourceInstanceFriendlyName(UpdateResourceInstanceNameCommand updateResourceInstanceFriendlyNameCommand, User updatingUser);

    void deleteResourceInstance(Identifier<ResourceInstance> aResourceInstanceId);
    
    String  encryptUsingPlatformBean(String cleartext);
}
