package com.cerner.jwala.service;

/**
 * Wrapper that contains information on a remote command execution.
 *
 * Created by Jedd Cuison on 3/25/2016
 */
public class RemoteCommandReturnInfo {

    public final int retCode;
    public final String standardOuput;
    public final String errorOutput;

    public RemoteCommandReturnInfo(final int retCode, final String standardOuput, final String errorOutput) {
        this.retCode = retCode;
        this.standardOuput = standardOuput;
        this.errorOutput = errorOutput;
    }

    @Override
    public String toString() {
        return "RemoteCommandReturnInfo{" +
                "retCode=" + retCode +
                ", standardOuput='" + standardOuput + '\'' +
                ", errorOutput='" + errorOutput + '\'' +
                '}';
    }

}
