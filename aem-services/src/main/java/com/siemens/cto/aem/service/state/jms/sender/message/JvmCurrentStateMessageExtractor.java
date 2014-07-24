package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

public class JvmCurrentStateMessageExtractor implements CurrentStateMessageExtractor {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    public CurrentState extract(final MapMessage aMessage) throws JMSException {
        return new CurrentState<>(getId(aMessage),
                                  getState(aMessage),
                                  getAsOf(aMessage),
                                  StateType.JVM);
    }

    Identifier<Jvm> getId(final MapMessage aMessage) throws JMSException {
        return new Identifier<>(aMessage.getString(CommonStateKey.ID.getKey()));
    }

    JvmState getState(final MapMessage aMessage) throws JMSException {
        return JvmState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }

    DateTime getAsOf(final MapMessage aMessage) throws JMSException {
        return DATE_TIME_FORMATTER.parseDateTime(aMessage.getString(CommonStateKey.AS_OF.getKey()));
    }
}
