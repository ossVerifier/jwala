package com.cerner.jwala.service.binarydistribution;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionLockManager {
    /**
     *
     * @param resourseName
     */
    public void writeLock(String resourseName);

    /**
     *
     * @param resourseName
     */
    public void writeUnlock(String resourseName);
}
