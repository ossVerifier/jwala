package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;

public class JvmDispatchCommand extends SplittableDispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final Jvm jvm;
    private final ControlJvmCommand command;

    public JvmDispatchCommand(Jvm theJvm, ControlJvmCommand theCommand) {
        jvm = theJvm;
        command = theCommand;
    }
    
    public Jvm getJvm() {
        return jvm;
    }

    public ControlJvmCommand getCommand() {
        return command;
    }

    @Override public String toString() {
        return "replace-with-equals-builder";
    }
}
