package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public class BinaryDistributionLockManagerImpl implements BinaryDistributionLockManager {

    private final Map<String, ReentrantReadWriteLock> binariesWriteLocks = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionLockManagerImpl.class);

    @Override
    public void writeLock(String resourceName) {
        if (!binariesWriteLocks.containsKey(resourceName)) {
            binariesWriteLocks.put(resourceName, new ReentrantReadWriteLock());
        }
        binariesWriteLocks.get(resourceName).writeLock().lock();
        LOGGER.info("Added write lock for resource {}", resourceName);
    }

    @Override
    public synchronized void writeUnlock(String resourceName) {
        if (binariesWriteLocks.containsKey(resourceName)) {
            binariesWriteLocks.get(resourceName).writeLock().unlock();
            LOGGER.info("Removed write lock for resource {}", resourceName);
        }
    }
}
