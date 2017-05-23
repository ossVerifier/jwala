package com.cerner.jwala.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Arvindo Kinny on 5/9/2017.
 */
public class JwalaUtil {
    public static final String INSTALL_SERVICE_PWD_PARAM = "svc_password=" ;
    public static final String INSTALL_SERVICE_PWD_PATTERN = INSTALL_SERVICE_PWD_PARAM+"(.*?)\\s";
    public static final String INSTALL_SERVICE_PWD_REPLACEMENT = INSTALL_SERVICE_PWD_PARAM+" =******** ";

    public static String scrubDomainUserPassword(String message){
        return StringUtils.replacePattern(message, INSTALL_SERVICE_PWD_PATTERN, INSTALL_SERVICE_PWD_REPLACEMENT);
    }

}
