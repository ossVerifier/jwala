package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;

public interface Rule {

    boolean isValid();

    void validate() throws BadRequestException;
}
