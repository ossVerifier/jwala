package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public class JvmDispatchCommand extends DispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final User user;
    private final Jvm jvm;
    private final ControlJvmCommand command;

    public JvmDispatchCommand(Jvm theJvm, ControlJvmCommand theCommand, User theUser) {
        jvm = theJvm;
        command = theCommand;
        user = theUser;
    }
    
    public Jvm getJvm() {
        return jvm;
    }

    public User getUser() {
        return user;
    }

    public ControlJvmCommand getCommand() {
        return command;
    }

    @Override public String toString() {
        return "replace-with-equals-builder";
    }
}
