package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JsonCreateApplication {

    public String webappName;
    public Long   groupId;
    public String context;
    
    public JsonCreateApplication() {  }

    public CreateApplicationCommand toCreateCommand() throws BadRequestException {
        return  new CreateApplicationCommand(
                    Identifier.id(groupId, Group.class),webappName,context);
    }

}
