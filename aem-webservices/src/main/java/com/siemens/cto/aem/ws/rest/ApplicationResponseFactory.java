package com.siemens.cto.aem.ws.rest;

import java.util.List;

import static com.siemens.cto.aem.ws.rest.ApplicationResponseStatus.INVALID_POST_DATA;
import static com.siemens.cto.aem.ws.rest.ApplicationResponseStatus.SUCCESS;

/**
 * Created by Z003BPEJ on 2/27/14.
 */
public class ApplicationResponseFactory {

    private ApplicationResponseFactory() {
        // prevent instantiation
    }

    /**
     * Build a "SUCCESSFUL" {@Link ApplicationResponse}
     * @param content {@Link ApplicationResponseContent}
     * @return {@Link ApplicationResponse}
     */
    public static final ApplicationResponse createApplicationResponse(ApplicationResponseContent content) {
        final ApplicationResponse applicationResponse =
                new ApplicationResponse(SUCCESS.getCode(), SUCCESS.toString(), content);
        return applicationResponse;
    }

    /**
     * Build an {@Link ApplicationResponse} with exception
     * @param sts
     * @param e
     * @return {@Link ApplicationResponse}
     */
    public static final ApplicationResponse createApplicationResponse(ApplicationResponseStatus sts, Exception e) {
        final String msg = e.getMessage();
        final ApplicationResponse applicationResponse = new ApplicationResponse(sts.getCode(), msg, null);
        return applicationResponse;
    }

    /**
     * Build a "INVALID POST DATA" {@link com.siemens.cto.aem.ws.rest.ApplicationResponse}
     * @param invalidPostDataList
     * @return {@Link ApplicationResponse}
     */
    public static final ApplicationResponse createInvalidPostDataApplicationResponse(List<String> invalidPostDataList) {
        final ApplicationResponse applicationResponse =
                new ApplicationResponse(INVALID_POST_DATA.getCode(), "Invalid parameters: " +
                        invalidPostDataList.toString(), null);
        return applicationResponse;
    }

}
