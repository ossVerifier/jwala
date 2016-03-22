package com.siemens.cto.aem.service.jvm.state;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);
    private static final String TOPIC_SERVER_STATES = "/topic/server-states";

    private final JvmService jvmService;
    private final SimpMessagingTemplate messagingTemplate;
    private JvmStateMapMessageConverterImpl converter = new JvmStateMapMessageConverterImpl();

    private static final Map<Identifier<Jvm>, JvmState> JVM_LAST_PERSISTED_STATE_MAP = new ConcurrentHashMap<>();
    private static final Map<Identifier<Jvm>, String> JVM_LAST_PERSISTED_ERROR_STATUS_MAP = new ConcurrentHashMap<>();

    private final GroupStateNotificationService groupStateNotificationService;

    public JvmStateReceiverAdapter(final JvmService jvmService, final SimpMessagingTemplate messagingTemplate,
                                   final GroupStateNotificationService groupStateNotificationService) {
        this.jvmService = jvmService;
        this.messagingTemplate = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
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
            final CurrentState currentState = new CurrentState(id, state, DateTime.now(), StateType.JVM, msg);
            logger.info("Processed JGroups, running update {}", message);
            jvmService.updateState(id, state, msg);
            messagingTemplate.convertAndSend(TOPIC_SERVER_STATES, currentState);
            groupStateNotificationService.retrieveStateAndSendToATopic(newState.getId(), Jvm.class, TOPIC_SERVER_STATES);
        }

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
    protected boolean isStateChangedAndOrMsgNotEmpty(CurrentState<Jvm, JvmState> newState) {
        boolean stateAndOrMsgChanged = false;

        if (!JVM_LAST_PERSISTED_STATE_MAP.containsKey(newState.getId()) ||
                !JVM_LAST_PERSISTED_STATE_MAP.get(newState.getId()).equals(newState.getState())) {
            JVM_LAST_PERSISTED_STATE_MAP.put(newState.getId(), newState.getState());
            stateAndOrMsgChanged = true;
        }

        if (StringUtils.isNotEmpty(newState.getMessage()) && (!JVM_LAST_PERSISTED_ERROR_STATUS_MAP.containsKey(newState.getId()) ||
                !JVM_LAST_PERSISTED_ERROR_STATUS_MAP.get(newState.getId()).equals(newState.getMessage()))) {
            JVM_LAST_PERSISTED_ERROR_STATUS_MAP.put(newState.getId(), newState.getMessage());
            stateAndOrMsgChanged = true;
        }
        return stateAndOrMsgChanged;
    }

}
