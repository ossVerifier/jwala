package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import com.siemens.cto.aem.common.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

public interface JvmStateMapMessageConverter {
    JvmStateMessage convert(MapMessage aMapMessage) throws JMSException;

    JvmStateMessage convert(Map<ReportingJmsMessageKey, String> messageMap);
}
