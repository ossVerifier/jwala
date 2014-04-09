package com.siemens.cto.aem.domain.model.rule.identifier;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;

public abstract class IdentifierRule<T> implements Rule {

    private final Identifier<T> id;
    private final MessageResponseStatus messageResponseStatus;
    private final String message;

    public IdentifierRule(final Identifier<T> theId,
                          final MessageResponseStatus theMessageResponseStatus,
                          final String theMessage) {
        id = theId;
        messageResponseStatus = theMessageResponseStatus;
        message = theMessage;
    }

    @Override
    public boolean isValid() {
        return id != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(messageResponseStatus,
                                          message);
        }
    }
}
