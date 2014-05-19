package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JsonUpdateApplication {

    public Long   webappId;
    public String newWebappName;
    public Long   newGroupId;
    public String newContext;
    
    public JsonUpdateApplication() {  }

    public UpdateApplicationCommand toUpdateCommand() throws BadRequestException {
        return  new UpdateApplicationCommand(
                    Identifier.id(webappId, Application.class),
                    Identifier.id(newGroupId, Group.class),newWebappName,newContext);
    }

}