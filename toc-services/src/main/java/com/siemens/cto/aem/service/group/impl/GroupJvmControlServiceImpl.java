package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import org.springframework.transaction.annotation.Transactional;

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
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("thread-task-executor.group-control.pool.size", "25")));
    }

    @Transactional
    @Override
    public void controlGroup(final ControlGroupJvmRequest controlGroupJvmRequest, final User aUser) {

        controlGroupJvmRequest.validate();

        Group group = groupService.getGroup(controlGroupJvmRequest.getGroupId());

        final Set<Jvm> jvms = group.getJvms();
        if (jvms != null) {
            for (final Jvm jvm : jvms) {
                executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        ControlJvmRequest controlJvmRequest = new ControlJvmRequest(jvm.getId(), controlGroupJvmRequest.getControlOperation());
                        return jvmControlService.controlJvm(controlJvmRequest, aUser);
                    }
                });
            }
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
        for (Group group : groupService.getGroups()) {
            ControlGroupJvmRequest controlAllJvmsRequest = new ControlGroupJvmRequest(group.getId(), controlGroupJvmRequest.getControlOperation());
            controlGroup(controlAllJvmsRequest, user);
        }
    }
}
