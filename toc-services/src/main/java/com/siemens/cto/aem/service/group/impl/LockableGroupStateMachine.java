package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.group.GroupStateMachine;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LockableGroupStateMachine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LockableGroupStateMachine.class);

    // both can be null to start with
    private GroupStateMachine delegate;
    Semaphore readLock;
    Semaphore writeLock;

    // tracks information across calls
    private boolean isDirty = false;

    // for multiple calls 
    private ReadWriteLease writeLease;

    public LockableGroupStateMachine() {
    }

    private synchronized void initializeLocks() {
        if (readLock == null) {
            readLock = new Semaphore(10);
            writeLock = new Semaphore(1);
        }
    }

    private void writeLock() {
        writeLock.acquireUninterruptibly();
        readLock.acquireUninterruptibly(10);
        LOGGER.trace("GSM Write locked " + this);
    }

    private boolean tryWriteLock(long timeout, TimeUnit units) throws InterruptedException {
        LOGGER.trace("GSM tryWrite lock " + this);
        if (writeLock.tryAcquire(1, timeout, units)) {
            readLock.acquireUninterruptibly(10);
            LOGGER.trace("GSM tryWrite locked " + this);
            return true;
        }
        LOGGER.warn("GSM tryWrite lock fail after 1 second " + this);
        return false;
    }

    private void writeUnlock() {
        LOGGER.trace("GSM Write unlock " + this);
        writeLease = null;  // remove writeLease
        readLock.release(10);
        writeLock.release();
        LOGGER.trace("GSM Write unlocked " + this);
    }

    private void readLock() {
        LOGGER.trace("GSM Read lock" + this);
        readLock.acquireUninterruptibly();
        LOGGER.trace("GSM Read locked" + this);
    }

    private void readUnlock() {
        LOGGER.trace("GSM Read unlock" + this);
        readLock.release();
        LOGGER.trace("GSM Read unlocked" + this);
    }

    private void downgradeLock() {
        LOGGER.trace("GSM Downgrade lock" + this);
        readLock.release(9);
        writeLock.release();
    }

    /**
     * @param initCallback
     * @return true if the writeLock was taken
     */
    private boolean initializeDelegate(LockableGroupStateMachine.Initializer initCallback) {
        if (this.delegate != null) {
            return false;
        }

        if (this.delegate == null) {
            writeLock();
        }

        if (this.delegate == null) {
            this.delegate = initCallback.initializeGroupStateMachine();
        }

        return true;
    }

    public ReadWriteLease tryPersistentLock(LockableGroupStateMachine.Initializer initCallback, long timeout, TimeUnit units) throws InterruptedException {
        initializeLocks();

        if (!tryWriteLock(timeout, units)) {
            return null;
        }

        if (writeLease != null) {
            // you have the lock, so you must already own writeLease
            return writeLease;
        }

        if (delegate == null) {
            delegate = initCallback.initializeGroupStateMachine();
        }

        writeLease = new ReadWriteLease(this.delegate, false);
        return writeLease;
        // writeLock is still yours on exit
    }

    public Lease lockForReadWithResources(LockableGroupStateMachine.Initializer initCallback) {
        initializeLocks();
        if (!initializeDelegate(initCallback)) {
            readLock();
        } else {
            downgradeLock();
        }
        return new ReadOnlyLease(delegate);
    }

    public ReadWriteLease lockForWriteWithResources(LockableGroupStateMachine.Initializer initCallback) {
        initializeLocks();

        // Note: initializeDelegate calls writeLock when initCallback is null. If initCallback is not null
        //       initializeDelegate returns false then we do a writeLock. In short, we always call writeLock
        //       here!
        // TODO: Verify if the logic described above is correct ?
        if (!initializeDelegate(initCallback)) {
            writeLock();
        }
        // this is not a persistent lock, should be called in a withResources call
        return new ReadWriteLease(delegate, true);
    }

    public void unlockPersistent() {
        LOGGER.debug("GSM Unlocking: " + delegate);
        writeUnlock();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    private class ReadOnlyLease extends ReadWriteLease {

        private ReadOnlyLease(GroupStateMachine delegate) {
            super(delegate, true /*autocloseable*/);
        }

        private void throwReadOnly() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("GSM Lease is read-only");
        }

        @Override
        public void synchronizedInitializeGroup(Group group, User user) {
            throwReadOnly();
        }


        @Override
        public CurrentGroupState signalReset(User user) {
            throwReadOnly();
            return null; // not reachable
        }

        @Override
        public CurrentGroupState signalStopRequested(User user) {
            throwReadOnly();
            return null; // not reachable
        }

        @Override
        public CurrentGroupState signalStartRequested(User user) {
            throwReadOnly();
            return null; // not reachable
        }

        @Override
        public boolean refreshState() {
            throwReadOnly();
            return false;
        }

        @Override
        public void jvmError(Identifier<Jvm> jvmId) {
            throwReadOnly();

        }

        @Override
        public void jvmStopped(Identifier<Jvm> jvmId) {
            throwReadOnly();

        }

        @Override
        public void jvmStarted(Identifier<Jvm> jvmId) {
            throwReadOnly();

        }

        @Override
        public void wsError(Identifier<WebServer> wsId) {
            throwReadOnly();

        }

        @Override
        public void wsReachable(Identifier<WebServer> wsId) {
            throwReadOnly();

        }

        @Override
        public void wsUnreachable(Identifier<WebServer> wsId) {
            throwReadOnly();

        }

        @Override
        public Lease readOnly() {
            // already readonly
            return this;
        }

        @Override
        public void releaseLock() {
            LockableGroupStateMachine.this.readUnlock();
        }

    }

    public interface Initializer {
        public GroupStateMachine initializeGroupStateMachine();
    }

    public abstract interface Lease extends GroupStateMachine, AutoCloseable {
        public Lease readOnly();

        public void releaseLock();
    }

    public class ReadWriteLease implements Lease {

        GroupStateMachine delegate;
        boolean autoRelease;
        boolean readUnlock = false;

        protected ReadWriteLease(GroupStateMachine delegate, boolean autoRelease) {
            this.delegate = delegate;
            this.autoRelease = autoRelease;
            if (!this.autoRelease) {
                LOGGER.debug("GSM Locked: " + delegate);
            }

        }

        @Override
        public void synchronizedInitializeGroup(Group group, User user) {
            delegate.synchronizedInitializeGroup(group, user);
        }

        @Override
        public CurrentGroupState signalReset(User user) {
            return delegate.signalReset(user);
        }

        @Override
        public CurrentGroupState signalStopRequested(User user) {
            return delegate.signalStopRequested(user);

        }

        @Override
        public CurrentGroupState signalStartRequested(User user) {
            return delegate.signalStartRequested(user);

        }

        @Override
        public boolean refreshState() {
            return delegate.refreshState();
        }

        @Override
        public void jvmError(Identifier<Jvm> jvmId) {
            delegate.jvmError(jvmId);

        }

        @Override
        public void jvmStopped(Identifier<Jvm> jvmId) {
            delegate.jvmStopped(jvmId);

        }

        @Override
        public void jvmStarted(Identifier<Jvm> jvmId) {
            delegate.jvmStarted(jvmId);
        }

        @Override
        public void wsError(Identifier<WebServer> wsId) {
            delegate.wsError(wsId);

        }

        @Override
        public void wsReachable(Identifier<WebServer> wsId) {
            delegate.wsReachable(wsId);
        }

        @Override
        public void wsUnreachable(Identifier<WebServer> wsId) {
            delegate.wsUnreachable(wsId);

        }

        @Override
        public boolean canStart() {
            return delegate.canStart();
        }

        @Override
        public boolean canStop() {
            return delegate.canStop();
        }

        @Override
        public GroupState getCurrentState() {
            return delegate.getCurrentState();

        }

        @Override
        public Group getCurrentGroup() {
            return delegate.getCurrentGroup();

        }

        @Override
        public CurrentGroupState getCurrentStateDetail() {
            return delegate.getCurrentStateDetail();

        }

        @Override
        public void close() throws Exception {
            if (autoRelease) {
                releaseLock();
            }
        }

        @Override
        public Lease readOnly() {
            if (readUnlock) {
                return this;
            }
            delegate = new ReadOnlyLease(delegate);
            LockableGroupStateMachine.this.downgradeLock();
            readUnlock = true;
            return this;
        }

        @Override
        public void releaseLock() {
            if (readUnlock) {
                ((ReadOnlyLease) delegate).releaseLock();
            } else {
                LockableGroupStateMachine.this.unlockPersistent();
            }
        }
    }
}