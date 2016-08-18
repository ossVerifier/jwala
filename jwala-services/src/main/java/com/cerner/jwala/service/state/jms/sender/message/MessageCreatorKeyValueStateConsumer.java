package com.cerner.jwala.service.state.jms.sender.message;

import org.springframework.jms.core.MessageCreator;

import com.cerner.jwala.common.domain.model.state.KeyValueStateConsumer;
import com.cerner.jwala.common.domain.model.state.message.StateKey;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageCreatorKeyValueStateConsumer implements KeyValueStateConsumer, MessageCreator {

    private final Map<StateKey, String> entries;

    public MessageCreatorKeyValueStateConsumer() {
        this(Collections.<StateKey, String>emptyMap());
    }

    MessageCreatorKeyValueStateConsumer(final Map<StateKey, String> aSourceMap) {
        entries = new HashMap<>(aSourceMap);
    }

    @Override
    public void set(final StateKey aKey,
                    final String aValue) {
        entries.put(aKey,
                    aValue);
    }

    @Override
    public Message createMessage(final Session aSession) throws JMSException {
        final MapMessage mapMessage = aSession.createMapMessage();
        for (final Map.Entry<StateKey, String> entry : entries.entrySet()) {
            mapMessage.setString(entry.getKey().getKey(),
                                 entry.getValue());
        }
        return mapMessage;
    }
}
