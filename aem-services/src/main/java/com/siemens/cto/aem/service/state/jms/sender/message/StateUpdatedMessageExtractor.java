package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;

public abstract class StateUpdatedMessageExtractor<T> {

    protected final MapMessage source;

    public StateUpdatedMessageExtractor(final Message theSource) throws JMSException {
        if (theSource instanceof MapMessage) {
            source = (MapMessage)theSource;
        } else {
            throw new JMSException("Unsupported JMS message type :" + theSource.getClass() + " with message : {" + theSource.toString() + "}");
        }
    }

    public abstract T extract() throws JMSException;

    protected <S> Identifier<S> extractId() throws JMSException{
        return new Identifier<>(getString(CommonStateKey.ID));
    }

    protected StateType extractStateType() throws JMSException {
        return StateType.valueOf(getString(CommonStateKey.TYPE));
    }

    protected DateTime extractAsOf() throws JMSException {
        return new DateTime(getString(CommonStateKey.AS_OF));
    }

    protected String getString(final StateKey aMessageKey) throws JMSException {
        return source.getString(aMessageKey.getKey());
    }
}
