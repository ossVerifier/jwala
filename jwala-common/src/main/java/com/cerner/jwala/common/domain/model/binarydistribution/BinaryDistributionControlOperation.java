package com.cerner.jwala.common.domain.model.binarydistribution;

import static com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionStatus.*;

/**
 * Created by LW044480 on 9/7/2016.
 */
public enum BinaryDistributionControlOperation {

    CHECK_FILE_EXISTS("checkFileExists", CHECK_FAIL, CHECK_SUCCESSFUL),
    CREATE_DIRECTORY("createDirectory", MKDIR_FAIL, MKDIR_SUCCESSFUL),
    SECURE_COPY("secureCopy", COPY_FAIL, COPY_SUCCESSFUL),
    DELETE_BINARY("deleteBinary", DELETE_FAIL, DELETE_SUCCESSFUL),
    UNZIP_BINARY("unzipBinary", UNZIP_FAIL, UNZIP_SUCCESSFUL),
    CHANGE_FILE_MODE("changeFileMode", CHANGE_MODE_FAIL, CHANGE_MODE_SUCCESSFUL);

    private final String operationValue;
    private final BinaryDistributionStatus fail, success;

    BinaryDistributionControlOperation(String operationValue, BinaryDistributionStatus fail, BinaryDistributionStatus success) {
        this.operationValue = operationValue;
        this.fail = fail;
        this.success = success;
    }

}
