package com.siemens.cto.aem.ws.rest;

/**
 * Created by Z003BPEJ on 2/24/14.
 */
public enum ApplicationResponseStatus {

    SUCCESS, RECORD_NOT_FOUND;

    public final String getCode() {
        return Integer.toString(this.ordinal());
    }

}
