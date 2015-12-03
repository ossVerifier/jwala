package com.siemens.cto.aem.common.exception;

public enum Success implements MessageResponseStatus {

    SUCCESS;

    @Override
    public String getMessageCode() {
        return "0";
    }

    @Override
    public String getMessage() {
        return "SUCCESS";
    }
}
