package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.Message;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

public class IdentifierStateUpdatedMessageExtractor<T> extends StateUpdatedMessageExtractor<Identifier<T>> {

    public IdentifierStateUpdatedMessageExtractor(final Message theSource) throws JMSException {
        super(theSource);
    }

    @Override
    public Identifier<T> extract() throws JMSException {
        return new Identifier<>(source.getString(CommonStateKey.ID.getKey()));
    }
}
