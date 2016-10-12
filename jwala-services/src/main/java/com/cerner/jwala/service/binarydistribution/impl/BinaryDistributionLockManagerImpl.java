package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by AK048646 on 10/11/2016.
 */
public class BinaryDistributionLockManagerImpl implements BinaryDistributionLockManager {

    private final Map<String, ReentrantReadWriteLock> binariesWriteLocks = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionLockManagerImpl.class);

    @Override
    public void writeLock(Object resourceName) {
        if (!binariesWriteLocks.containsKey(resourceName.toString())) {
            binariesWriteLocks.put(resourceName.toString(), new ReentrantReadWriteLock());
        }
        binariesWriteLocks.get(resourceName.toString()).writeLock().lock();
        LOGGER.info("Added write lock for resource {}", resourceName.toString());
    }

    @Override
    public void writeUnlock(Object resourceName) {
        if (binariesWriteLocks.containsKey(resourceName.toString())) {
            binariesWriteLocks.get(resourceName.toString()).writeLock().unlock();
            LOGGER.info("Removed write lock for resource {}", resourceName.toString());
        }
    }
}
