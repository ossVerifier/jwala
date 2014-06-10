package com.siemens.cto.aem.service.jvm.state.jms.listener.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;

public interface JvmStateMapMessageConverter {
    JvmStateMessage convert(MapMessage aMapMessage) throws JMSException;
}
