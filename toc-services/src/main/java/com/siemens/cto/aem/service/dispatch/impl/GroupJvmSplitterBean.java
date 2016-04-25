package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.common.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupJvmSplitterBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmSplitterBean.class);

    private final JvmPersistenceService jvmPersistenceService;

    public GroupJvmSplitterBean(final JvmPersistenceService jvmPersistenceService) {
        this.jvmPersistenceService = jvmPersistenceService;
    }

    public List<JvmDispatchCommand> split(final GroupJvmDispatchCommand groupDispatchCommand) {
        final Group group = groupDispatchCommand.getGroup();

        final Collection<Jvm> jvmCollection;
        if (group == null) {
            jvmCollection = jvmPersistenceService.getJvms();
        } else {
            jvmCollection = group.getJvms();
        }

        final String groupDesc = group == null ? "all groups" : group.getName() + " group";
        LOGGER.debug("Splitting groupDispatchCommand for {}: START", groupDesc);

        final List<JvmDispatchCommand> jvmCommands = new ArrayList<>();
        for (final Jvm jvm : jvmCollection) {
            jvmCommands.add(new JvmDispatchCommand(jvm, groupDispatchCommand));
            LOGGER.debug("Created dispatch command for JVM {}", jvm.getJvmName());
        }
        LOGGER.debug("Splitting groupDispatchCommand for {}: END", groupDesc);
        return jvmCommands;
    }
}
