package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

public class JvmStateMapMessageConverterImpl implements JvmStateMapMessageConverter {

    @Override
    public JvmStateMessage convert(final MapMessage aMapMessage) throws JMSException {
        return new JvmStateMessage(get(aMapMessage, ReportingJmsMessageKey.ID),
                get(aMapMessage, ReportingJmsMessageKey.INSTANCE_ID),
                get(aMapMessage, ReportingJmsMessageKey.TYPE),
                get(aMapMessage, ReportingJmsMessageKey.STATE),
                get(aMapMessage, ReportingJmsMessageKey.AS_OF));
    }

    protected String get(final MapMessage aMapMessage,
                         final ReportingJmsMessageKey aKey) throws JMSException {
        return aMapMessage.getString(aKey.getKey());
    }

    @Override
    public JvmStateMessage convert(Map<ReportingJmsMessageKey, String> messageMap) {
        return new JvmStateMessage(messageMap.get(ReportingJmsMessageKey.ID),
                messageMap.get(ReportingJmsMessageKey.INSTANCE_ID),
                messageMap.get(ReportingJmsMessageKey.TYPE),
                messageMap.get(ReportingJmsMessageKey.STATE),
                messageMap.get(ReportingJmsMessageKey.AS_OF));
    }
}
