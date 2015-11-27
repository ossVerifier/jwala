package com.siemens.cto.aem.domain.command.group;

import com.siemens.cto.aem.domain.command.Command;

public interface GroupCommand extends Command {
    
    Long getId();
    
    String getExternalOperationName();
    
    String getType();

}
