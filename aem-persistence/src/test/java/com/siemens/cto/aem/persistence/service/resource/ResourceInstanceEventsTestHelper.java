package com.siemens.cto.aem.persistence.service.resource;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class ResourceInstanceEventsTestHelper {

    public static Event<CreateResourceInstanceCommand> createCreateResourceInstanceCommand(final String resourceTypeName, String friendlyName, final Long groupId, final Map<String, String> attributes, final String userName) {
       return new Event<>(new CreateResourceInstanceCommand(resourceTypeName, friendlyName, groupId, attributes), AuditEvent.now(new User(userName)));
    }
    public static Event<UpdateResourceInstanceAttributesCommand> createUpdateResourceInstanceAttributesCommand(final Identifier<ResourceInstance> aResourceInstanceId, final Map<String, String> attributes, String userName) {
        return new Event<>(new UpdateResourceInstanceAttributesCommand(aResourceInstanceId, attributes), AuditEvent.now(new User(userName)));
    }
    public static Event<UpdateResourceInstanceNameCommand> createUpdateResourceInstanceFriendlyNameCommand(final Identifier<ResourceInstance> aResourceInstanceId, final String friendlyName, String userName) {
        return new Event<>(new UpdateResourceInstanceNameCommand(aResourceInstanceId, friendlyName), AuditEvent.now(new User(userName)));
    }

}
