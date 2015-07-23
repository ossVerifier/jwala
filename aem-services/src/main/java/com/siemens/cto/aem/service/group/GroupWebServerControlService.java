package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;

import java.util.List;

public interface GroupWebServerControlService {

    GroupControlHistory controlGroup(final ControlGroupWebServerCommand aCommand, final User aUser);
    
    void dispatchCommandComplete(List<WebServerDispatchCommandResult> results);

}
