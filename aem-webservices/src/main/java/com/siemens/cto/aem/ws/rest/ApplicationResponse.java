package com.siemens.cto.aem.ws.rest;

/**
 * Created by z003bpej on 2/21/14.
 */
public class ApplicationResponse {

    private final String msgCode;
    private final String message;
    private final ApplicationResponseContent applicationResponseContent;

    public ApplicationResponse(String msgCode,
                               String message,
                               ApplicationResponseContent applicationResponseContent) {
        this.msgCode = msgCode;
        this.message = message;
        this.applicationResponseContent = applicationResponseContent;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public String getMessage() {
        return message;
    }

    public ApplicationResponseContent getApplicationResponseContent() {
        return applicationResponseContent;
    }

}
