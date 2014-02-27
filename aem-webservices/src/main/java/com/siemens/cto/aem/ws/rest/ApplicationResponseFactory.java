package com.siemens.cto.aem.ws.rest;

import static com.siemens.cto.aem.ws.rest.ApplicationResponseStatus.SUCCESS;

/**
 * Created by Z003BPEJ on 2/27/14.
 */
public class ApplicationResponseFactory {

    private ApplicationResponseFactory() {
        // prevent instantiation
    }

    /**
     * Build a "successful" {@link com.siemens.cto.aem.ws.rest.ApplicationResponse}
     * @param content {@link com.siemens.cto.aem.ws.rest.ApplicationResponseContent}
     * @return {@link com.siemens.cto.aem.ws.rest.ApplicationResponse}
     */
    public static final ApplicationResponse createApplicationResponse(ApplicationResponseContent content) {
        final ApplicationResponse applicationResponse =
                new ApplicationResponse(SUCCESS.getCode(), SUCCESS.toString(), content);
        return applicationResponse;
    }

    /**
     * Build an {@link com.siemens.cto.aem.ws.rest.ApplicationResponse} with exception
     * @param sts
     * @param e
     * @return {@link com.siemens.cto.aem.ws.rest.ApplicationResponse}
     */
    public static final ApplicationResponse createApplicationResponse(ApplicationResponseStatus sts, Exception e) {
        final String msg = e.getMessage();
        final ApplicationResponse applicationResponse = new ApplicationResponse(sts.getCode(), msg, null);
        return applicationResponse;
    }

}
