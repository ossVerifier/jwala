package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.domain.command.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.domain.command.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

import java.util.ArrayList;
import java.util.List;

public class GroupJvmSplitterBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmSplitterBean.class);

    public List<JvmDispatchCommand> split(GroupJvmDispatchCommand groupDispatchCommand) {
        Group group = groupDispatchCommand.getGroup();
        LOGGER.debug("splitting GroupJvmDispatchCommand for group {}", group.getName());

        List<JvmDispatchCommand> jvmCommands = new ArrayList<JvmDispatchCommand>();

        for (Jvm jvm : group.getJvms()) {
            jvmCommands.add(new JvmDispatchCommand(jvm, groupDispatchCommand));
            LOGGER.debug("Created dispatch command for JVM {}", jvm.getJvmName());
        }

        LOGGER.debug("split complete for GroupJvmDispatchCommand for group {}", group.getName());
        return jvmCommands;
    }
}
