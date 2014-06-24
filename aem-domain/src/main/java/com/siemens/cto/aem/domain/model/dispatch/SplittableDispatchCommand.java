package com.siemens.cto.aem.domain.model.dispatch;

import java.util.ArrayList;
import java.util.List;


/**
 * A command that can be broken down into constituent commands
 * A helper method is defined that implements a double 
 * dispatch pattern, forcing a mechanism for using a 
 * separate transformer to generate the sub-commands.
 * 
 * Intended to simplify Splitter definition
 * 
 * @author horspe00
 *
 */
public abstract class SplittableDispatchCommand extends DispatchCommand {    
        
    private static final long serialVersionUID = 1L;

    private long correlationId;
    
    public List<DispatchCommand> getSubCommands(SplitterTransformer splitter) { 
        // callback to SplitterTransformer to actually do the split work is recommended, 
        // dependency injection can be done there.
        return new ArrayList<DispatchCommand>();
    }
    
    public long getIdentity() {
        return correlationId;
    }

}
