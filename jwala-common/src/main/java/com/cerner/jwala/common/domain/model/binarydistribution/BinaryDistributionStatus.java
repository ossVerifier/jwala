package com.cerner.jwala.common.domain.model.binarydistribution;

/**
 * Created by LW044480 on 9/7/2016.
 */
public enum BinaryDistributionStatus {

    CHECK_FAIL("check fail"),
    CHECK_SUCCESSFUL("check successful"),
    COPY_FAIL("copy fail"),
    COPY_SUCCESSFUL("copy successful"),
    DELETE_FAIL("delete fail"),
    DELETE_SUCCESSFUL("delete successful"),
    MKDIR_FAIL("mkdir fail"),
    MKDIR_SUCCESSFUL("mkdir successful"),
    UNZIP_FAIL("unzip fail"),
    UNZIP_SUCCESSFUL("unzip successful");

    private String message;
    BinaryDistributionStatus(String message) {
        this.message = message;
    }

}
