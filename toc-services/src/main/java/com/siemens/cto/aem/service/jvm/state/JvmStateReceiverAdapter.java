package com.siemens.cto.aem.service.jvm.state;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.jvm.state.jms.listener.message.JvmStateMapMessageConverterImpl;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The listener for JGroup messages.
 */
public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);

    private JvmStateMapMessageConverterImpl converter = new JvmStateMapMessageConverterImpl();
    private final JvmStateService jvmStateService;

    public JvmStateReceiverAdapter(final JvmStateService jvmStateService) {
        this.jvmStateService = jvmStateService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Message jgroupMessage) {
        final Address src = jgroupMessage.getSrc();
        final Map<ReportingJmsMessageKey, String> messageMap = (Map<ReportingJmsMessageKey, String>) jgroupMessage.getObject();
        logger.debug("Received JGroups JVM state message {} {}", src, messageMap);

        final JvmStateMessage message = converter.convert(messageMap);

        // We don't report the "JVM" stopped state since it does not mean that the service has also stopped running.
        // JvmControlServiceImpl is the one that knows if the service has stopped and thus has the responsibility of
        // notifying the client.
        if (!JvmState.JVM_STOPPED.toString().equalsIgnoreCase(message.getState())) {
            final SetStateRequest<Jvm, JvmState> setStateCommand = message.toCommand();
            final CurrentState<Jvm, JvmState> newState = setStateCommand.getNewState();
            jvmStateService.updateState(newState.getId(), newState.getState(), newState.getMessage());
        }
    }

    @Override
    public void viewAccepted(View view) {
        logger.debug("JGroups coordinator cluster VIEW: {}", view.toString());
    }
}
