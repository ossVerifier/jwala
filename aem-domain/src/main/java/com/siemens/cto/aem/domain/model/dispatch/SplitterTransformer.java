package com.siemens.cto.aem.domain.model.dispatch;

import java.util.List;


/**
 * Interface for Spring 
 * Integration splitters that will split
 * Dispatch Commands.  
 *
 */
public interface SplitterTransformer {

    List<DispatchCommand> splitGroupToJvmCommands(GroupDispatchCommand groupDispatchCommand);

    List<DispatchCommand> splitGroupToDeployCommands(GroupDispatchCommand groupDispatchCommand);

}
