package com.siemens.cto.aem.service.jvm.state;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);

    private final JvmService jvmService;
    private final MessagingService messagingTemplate;
    private JvmStateMapMessageConverterImpl converter = new JvmStateMapMessageConverterImpl();
    private final GroupStateNotificationService groupStateNotificationService;
    private final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService;

    public JvmStateReceiverAdapter(final JvmService jvmService, final MessagingService messagingService,
                                   final GroupStateNotificationService groupStateNotificationService,
                                   final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService) {
        this.jvmService = jvmService;
        this.messagingTemplate = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
    }

    @Override
    public void receive(Message jgroupMessage) {
        final Address src = jgroupMessage.getSrc();
        final Map<ReportingJmsMessageKey, String> messageMap = (Map<ReportingJmsMessageKey, String>) jgroupMessage.getObject();
        logger.debug("Received JGroups JVM state message {} {}", src, messageMap);

        final JvmStateMessage message = converter.convert(messageMap);
        final SetStateRequest<Jvm, JvmState> setStateCommand = message.toCommand();
        final CurrentState<Jvm, JvmState> newState = setStateCommand.getNewState();
        if (isStateChangedAndOrMsgNotEmpty(newState)) {
            final String msg = newState.getMessage();
            final JvmState state = newState.getState();
            final Identifier<Jvm> id = newState.getId();
            final CurrentState currentState = new CurrentState<>(id, state, DateTime.now(), StateType.JVM, msg);
            logger.info("Processed JGroups, running update {}", message);
            jvmService.updateState(id, state, msg);
            messagingTemplate.send(currentState);
            groupStateNotificationService.retrieveStateAndSendToATopic(newState.getId(), Jvm.class);
        }
        // Always update the JVM state map even if the state did not change since there's another thread that checks if the state is stale of not!
        inMemoryStateManagerService.put(newState.getId(), newState);
    }

    @Override
    public void viewAccepted(View view) {
        logger.debug("JGroups coordinator cluster VIEW: {}", view.toString());
    }

    /**
     * Check if the state has changed and-or message is not empty.
     *
     * @param newState the latest state
     * @return returns true if the state is not the same compared to the previous state or if there's a message (error message)
     */
    private boolean isStateChangedAndOrMsgNotEmpty(CurrentState<Jvm, JvmState> newState) {
        boolean stateAndOrMsgChanged = false;

        if (!inMemoryStateManagerService.containsKey(newState.getId()) ||
                !inMemoryStateManagerService.get(newState.getId()).getState().equals(newState.getState())) {
            stateAndOrMsgChanged = true;
        }

        if (StringUtils.isNotEmpty(newState.getMessage()) && (!inMemoryStateManagerService.containsKey(newState.getId()) ||
                !inMemoryStateManagerService.get(newState.getId()).getMessage().equals(newState.getMessage()))) {
            stateAndOrMsgChanged = true;
        }
        return stateAndOrMsgChanged;
    }

}
