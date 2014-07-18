package com.siemens.cto.aem.domain.model.group.command;

import com.siemens.cto.aem.domain.model.command.Command;

public interface GroupCommand extends Command {
    
    Long getId();
    
    String getExternalOperationName();
    
    String getType();

}
