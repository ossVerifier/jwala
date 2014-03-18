package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;

public abstract class ValidNameRule implements Rule {

    protected final String name;

    public ValidNameRule(final String theName) {
        name = theName;
    }

    @Override
    public boolean isValid() {
        return (name != null) && (!"".equals(name.trim()));
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(getMessageResponseStatus(),
                                          getMessage());
        }
    }

    protected abstract MessageResponseStatus getMessageResponseStatus();

    protected abstract String getMessage();
}
