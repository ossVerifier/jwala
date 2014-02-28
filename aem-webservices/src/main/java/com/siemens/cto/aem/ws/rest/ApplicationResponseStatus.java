package com.siemens.cto.aem.ws.rest;

/**
 * Created by Z003BPEJ on 2/24/14.
 */
public enum ApplicationResponseStatus {

    SUCCESS, RECORD_NOT_FOUND, RECORD_NOT_ADDED, RECORD_NOT_UPDATED, RECORD_NOT_DELETED, INVALID_POST_DATA;

    public final String getCode() {
        return Integer.toString(this.ordinal());
    }

}
