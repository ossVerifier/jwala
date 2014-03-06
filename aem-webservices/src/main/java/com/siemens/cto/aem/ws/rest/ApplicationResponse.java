package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

/**
 * Created by z003bpej on 2/21/14.
 */
public class ApplicationResponse {

    private final MessageResponseStatus responseStatus;
    private final Object applicationResponseContent;

    public ApplicationResponse(final MessageResponseStatus theMessageResponseStatus,
                               final Object theContent) {
        responseStatus = theMessageResponseStatus;
        applicationResponseContent = theContent;
    }

    @Deprecated
    public ApplicationResponse(final String msgCode,
                               final String message,
                               final ApplicationResponseContent applicationResponseContent) {
        this(new MessageResponseStatus() {
                @Override
                public String getMessageCode() {
                    return msgCode;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            },
             applicationResponseContent);
    }

    public String getMsgCode() {
        return responseStatus.getMessageCode();
    }

    public String getMessage() {
        return responseStatus.getMessage();
    }

    public Object getApplicationResponseContent() {
        return applicationResponseContent;
    }

}
