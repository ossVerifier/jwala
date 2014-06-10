package com.siemens.cto.aem.service.jvm.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessageKey;

public class JvmStateUpdatedMessageFiller {

    private MapMessage message;

    public JvmStateUpdatedMessageFiller(final MapMessage aMessage) {
        message = aMessage;
    }

    public JvmStateUpdatedMessageFiller fill(final Identifier<Jvm> aJvmId) throws JMSException {
        message.setString(JvmStateMessageKey.JVM_ID.getKey(), aJvmId.getId().toString());
        return this;
    }
}
