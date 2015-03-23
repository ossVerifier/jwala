package com.siemens.cto.aem.persistence.dao.resource;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

import java.util.Map;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class ResourceInstanceEventsTestHelper {

    public static Event<CreateResourceInstanceCommand> createCreateResourceInstanceCommand(final String resourceTypeName, final Long parentId, final String parentType, final Map<String, String> attributes, final String userName) {
       return new Event<>(new CreateResourceInstanceCommand(resourceTypeName, parentId, parentType, attributes), AuditEvent.now(new User(userName)));
    }
    public static Event<UpdateResourceInstanceCommand> createUpdateResourceInstanceCommand(final Identifier<ResourceInstance> aResourceInstanceId, final Map<String, String> attributes, String userName) {
        return new Event<>(new UpdateResourceInstanceCommand(aResourceInstanceId, attributes), AuditEvent.now(new User(userName)));
    }

}
