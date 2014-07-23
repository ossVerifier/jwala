package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;

public abstract class CurrentStateJmsMessageSerializer<T extends CurrentState<?,?>> implements JmsMessageSerializer<T> {

    private final DateTimeFormatter dateTimeFormatter;

    protected final T currentState;

    public CurrentStateJmsMessageSerializer(final T theCurrentState) {
        this(theCurrentState,
             ISODateTimeFormat.dateTime());
    }

    protected CurrentStateJmsMessageSerializer(final T theCurrentState,
                                               final DateTimeFormatter theDateTimeFormatter) {
        currentState = theCurrentState;
        dateTimeFormatter = theDateTimeFormatter;
    }

    @Override
    public T extract(final Message aMessage) throws JMSException {
        if (aMessage instanceof MapMessage) {
            return extractFromMapMessage((MapMessage)aMessage);
        } else {
            throw new JMSException("Unsupported JMS message type :" + aMessage.getClass() + " with message : {" + aMessage.toString() + "}");
        }
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {

        final MapMessage message = session.createMapMessage();
        fill(message);
        fillAdditional(message);
        return message;
    }

    protected abstract T extractFromMapMessage(final MapMessage aMessage) throws JMSException;

    protected abstract StateType getType();

    protected abstract void fillAdditional(final MapMessage aMessage) throws JMSException;

    private Identifier<?> extractId(final MapMessage aMessage) throws JMSException {
        return new Identifier<>(get(aMessage, CommonStateKey.ID));
    }

    private DateTime extractDateTime(final MapMessage aMessage) throws JMSException {
        return dateTimeFormatter.parseDateTime(get(aMessage, CommonStateKey.AS_OF));
    }

    private void fill(final MapMessage aMessage) throws JMSException {
        set(aMessage, CommonStateKey.ID, currentState.getId().toString());
        set(aMessage, CommonStateKey.TYPE, getType().toString());
        set(aMessage, CommonStateKey.AS_OF, serializeDateTime(currentState.getAsOf()));
    }

    private String serializeDateTime(final DateTime aDateTime) {
        return dateTimeFormatter.print(aDateTime);
    }

    private String get(final MapMessage aMessage,
                       final StateKey aKey) throws JMSException {
        return aMessage.getString(aKey.getKey());
    }

    private void set(final MapMessage aMessage,
                     final StateKey aKey,
                     final String aValue) throws JMSException {
        aMessage.setString(aKey.getKey(),
                           aValue);
    }
}
