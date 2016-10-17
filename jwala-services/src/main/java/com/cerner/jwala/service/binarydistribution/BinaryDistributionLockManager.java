package com.cerner.jwala.service.binarydistribution;

/**
 * Created by AK048646 on 10/11/2016.
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
