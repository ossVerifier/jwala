package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

/**
 * Created by z003bpej on 6/3/14.
 */
class JsonUtilJvm {

    /**
     * Convert a string to an Integer.
     * Return null if the string cannot be converted.
     * @param val the string value to convert
     * @return Integer. Null if conversion fails.
     */
    static final Integer stringToInteger(final String val) {
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
