package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;

public interface Rule {

    boolean isValid();

    void validate() throws BadRequestException;

}
