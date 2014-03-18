package com.siemens.cto.aem.domain.model.command;

import com.siemens.cto.aem.common.exception.BadRequestException;

public interface Command {

    void validateCommand() throws BadRequestException;
}
