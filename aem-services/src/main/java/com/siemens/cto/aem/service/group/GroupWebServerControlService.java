package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.command.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.command.webserver.ControlGroupWebServerCommand;

import java.util.List;

public interface GroupWebServerControlService {

    void controlGroup(final ControlGroupWebServerCommand aCommand, final User aUser);
    
    void dispatchCommandComplete(List<WebServerDispatchCommandResult> results);

}
