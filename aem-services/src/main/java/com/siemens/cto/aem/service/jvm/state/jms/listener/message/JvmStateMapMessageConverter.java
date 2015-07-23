package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public interface JvmStateMapMessageConverter {
    JvmStateMessage convert(MapMessage aMapMessage) throws JMSException;
}
