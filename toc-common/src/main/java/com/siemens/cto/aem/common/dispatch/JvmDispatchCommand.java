package com.siemens.cto.aem.common.dispatch;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

public class JvmDispatchCommand extends DispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final GroupJvmDispatchCommand groupJvmDispatchCommand;
    private final Jvm jvm;

    public JvmDispatchCommand(Jvm theJvm, GroupJvmDispatchCommand theGroupJvmDispatchCommand) {
        jvm = theJvm;
        groupJvmDispatchCommand = theGroupJvmDispatchCommand;
    }
    
    public Jvm getJvm() {
        return jvm;
    }

    public GroupJvmDispatchCommand getGroupJvmDispatchCommand() {
        return groupJvmDispatchCommand;
    }

    @Override
    public String toString() {
        return "JvmDispatchCommand [groupJvmDispatchCommand=" + groupJvmDispatchCommand + ", jvm=" + jvm + "]";
    }
}
