package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.dispatch.JvmDispatchCommandResult;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmControlService;

import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupJvmControlServiceImpl implements GroupJvmControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmControlServiceImpl.class);

    private final GroupService groupService;
    private final JvmControlService jvmControlService;
    private final ExecutorService executorService;

    public GroupJvmControlServiceImpl(final GroupService theGroupService, final JvmControlService theJvmControlService) {
        groupService = theGroupService;
        jvmControlService = theJvmControlService;
        executorService = Executors.newFixedThreadPool(ApplicationProperties.getAsInteger("thread-task-executor.group-control.pool.size", 25));
    }

    @Transactional
    @Override
    public void controlGroup(final ControlGroupJvmRequest controlGroupJvmRequest, final User aUser) {

        controlGroupJvmRequest.validate();

        Group group = groupService.getGroup(controlGroupJvmRequest.getGroupId());

        final Set<Jvm> jvms = group.getJvms();
        if (jvms != null) {
            controlJvms(controlGroupJvmRequest, aUser, jvms);
        }
    }

    @Transactional
    public void dispatchCommandComplete(List<JvmDispatchCommandResult> results) {

        LOGGER.debug("entering dispatchCommandComplete with results {}", results);

        if (results != null && !results.isEmpty()) {

            long successCount = 0;
            long totalCount = 0;

            for (JvmDispatchCommandResult jvmDispatchCommandResult : results) {
                jvmDispatchCommandResult.getGroupJvmDispatchCommand();
                if (jvmDispatchCommandResult.wasSuccessful()) {
                    successCount++;
                }
                ++totalCount;
            }

            String logMsg = "Group Dispatch : Command Complete: " + successCount + " of " + totalCount + " succeeded.";
            if (successCount == results.size()) {
                LOGGER.info(logMsg);
            } else {
                LOGGER.warn(logMsg);
            }
        }
    }

    @Override
    public void controlAllJvms(final ControlGroupJvmRequest controlGroupJvmRequest, final User user) {
        Set<Jvm> jvms = new HashSet<>();
        for (Group group : groupService.getGroups()) {
            Set<Jvm> groupsJvms = group.getJvms();
            if (groupsJvms != null && !groupsJvms.isEmpty()) {
                jvms.addAll(groupsJvms);
            }
        }

        controlJvms(controlGroupJvmRequest, user, jvms);
    }

    private void controlJvms(final ControlGroupJvmRequest controlGroupJvmRequest, final User user, Set<Jvm> jvms) {
        for (final Jvm jvm : jvms) {
            executorService.submit(new Callable<CommandOutput>() {
                @Override
                public CommandOutput call() throws Exception {
                    ControlJvmRequest controlJvmRequest = new ControlJvmRequest(jvm.getId(), controlGroupJvmRequest.getControlOperation());
                    return jvmControlService.controlJvm(controlJvmRequest, user);
                }
            });
        }
    }
}
