package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JvmControlServiceImpl implements JvmControlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmControlServiceImpl.class);
    private static final String FORCED_STOPPED = "FORCED STOPPED";
    private final JvmService jvmService;
    private final RemoteCommandExecutor<JvmControlOperation> remoteCommandExecutor;
    private final HistoryService historyService;
    private final StateService<Jvm, JvmState> jvmStateService;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final RemoteCommandExecutor<JvmControlOperation> theExecutor,
                                 final HistoryService historyService,
                                 final StateService<Jvm, JvmState> jvmStateService) {
        jvmService = theJvmService;
        remoteCommandExecutor = theExecutor;
        this.historyService = historyService;
        this.jvmStateService = jvmStateService;
    }

    @Override
    public CommandOutput controlJvm(ControlJvmRequest controlJvmRequest, User aUser) {
        final Jvm jvm = jvmService.getJvm(controlJvmRequest.getJvmId());
        try {
            final String event = controlJvmRequest.getControlOperation().getOperationState() == null ?
                    controlJvmRequest.getControlOperation().name() : controlJvmRequest.getControlOperation().getOperationState().toStateString();
            historyService.createHistory(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), event, EventType.USER_ACTION, aUser.getId());

            CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(jvm.getJvmName(), jvm.getHostName(),
                    controlJvmRequest.getControlOperation(), new WindowsJvmPlatformCommandProvider());

            // Process other return codes...
            if (commandOutput.getReturnCode().getReturnCode() == ExecReturnCode.STP_EXIT_PROCESS_KILLED) {
                commandOutput = new CommandOutput(new ExecReturnCode(0), FORCED_STOPPED, commandOutput.getStandardError());
                final JvmSetStateRequest jvmSetStateRequest = new JvmSetStateCommandBuilder().setJvmId(controlJvmRequest.getJvmId())
                                                                                             .setJvmState(JvmState.SVC_STOPPED)
                                                                                             .setMessage(FORCED_STOPPED)
                                                                                             .build();
                jvmStateService.setCurrentState(jvmSetStateRequest, aUser);
            }

            return commandOutput;
        } catch (final CommandFailureException cfe) {
            LOGGER.error(cfe.getMessage(), cfe);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a JVM: " + controlJvmRequest, cfe);
        }
    }

    @Override
    public CommandOutput secureCopyFile(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath) throws CommandFailureException {
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jvm = jvmService.getJpaJvm(jvmId, true);

        return remoteCommandExecutor.executeRemoteCommand(
                jvm.getName(),
                jvm.getHostName(),
                secureCopyRequest.getControlOperation(),
                new WindowsJvmPlatformCommandProvider(),
                sourcePath,
                destPath);
    }

    @Override
    public CommandOutput secureCopyFileWithBackup(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath) throws CommandFailureException {
        // back up the original file first
        final Identifier<Jvm> jvmId = secureCopyRequest.getJvmId();
        final JpaJvm jvm = jvmService.getJpaJvm(jvmId, true);

        String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
        final String destPathBackup = destPath + currentDateSuffix;
        final CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                jvm.getName(),
                jvm.getHostName(),
                secureCopyRequest.getControlOperation(),
                new WindowsJvmPlatformCommandProvider(),
                destPath,
                destPathBackup);
        if (!commandOutput.getReturnCode().wasSuccessful()) {
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + jvm);
        }
        return secureCopyFile(secureCopyRequest, sourcePath, destPath);
    }

}
