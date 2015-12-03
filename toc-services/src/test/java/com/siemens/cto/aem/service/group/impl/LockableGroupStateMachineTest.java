package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.service.group.GroupStateMachine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by JC043760 on 11/18/2015.
 */
public class LockableGroupStateMachineTest {

    private LockableGroupStateMachine lockableGroupStateMachine;

    @Mock
    private LockableGroupStateMachine.Initializer initializer;

    private static final long TIMEOUT = 5000;

    @Mock
    private GroupStateMachine groupStateMachine;

    @Before
    public void setup() {
        lockableGroupStateMachine = new LockableGroupStateMachine();

        // I know this is less than ideal but I don't want to refactor {@link LockableGroupStateMachine}
        // while writing this test.
        // TODO: Refactor this once {@link LockableGroupStateMachine}
        lockableGroupStateMachine.readLock = mock(Semaphore.class);
        lockableGroupStateMachine.writeLock = mock(Semaphore.class);

        // final LockableGroupStateMachine.Initializer initializer = mock(LockableGroupStateMachine.Initializer.class);
        MockitoAnnotations.initMocks(this);

        when(initializer.initializeGroupStateMachine()).thenReturn(groupStateMachine);
    }

    @Test
    public void testTryPersistentLockWithWriteLockFail() throws InterruptedException {
        when(lockableGroupStateMachine.writeLock.tryAcquire(eq(1), eq(TIMEOUT), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(false);

        assertNull(lockableGroupStateMachine.tryPersistentLock(initializer, TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testTryPersistentLock() throws InterruptedException {
        when(lockableGroupStateMachine.writeLock.tryAcquire(anyInt(), eq(TIMEOUT), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);

        final LockableGroupStateMachine.ReadWriteLease theLease =
                lockableGroupStateMachine.tryPersistentLock(initializer, TIMEOUT, TimeUnit.MILLISECONDS);
        assertNotNull(theLease);
        verify(lockableGroupStateMachine.readLock).acquireUninterruptibly(10);

        reset(initializer);
        lockableGroupStateMachine.tryPersistentLock(initializer, TIMEOUT, TimeUnit.MILLISECONDS);
        verify(initializer, new Times(0)).initializeGroupStateMachine();
    }

    @Test
    public void testLockForReadWithResources() {
        Object theLease =
                lockableGroupStateMachine.lockForReadWithResources(initializer);
        assertNotNull(theLease);

        verify(lockableGroupStateMachine.readLock).release(9);
        verify(lockableGroupStateMachine.writeLock).release();

        reset(lockableGroupStateMachine.readLock);

        theLease = lockableGroupStateMachine.lockForReadWithResources(initializer);
        assertNotNull(theLease);
        verify(lockableGroupStateMachine.readLock).acquireUninterruptibly();
        verify(lockableGroupStateMachine.readLock, new Times(0)).release(9);
        verify(lockableGroupStateMachine.writeLock).release();
    }

    @Test
    public void testLockForWriteWithResources() {
        Object lease = lockableGroupStateMachine.lockForWriteWithResources(initializer);
        assertNotNull(lease);
        verify(lockableGroupStateMachine.writeLock).acquireUninterruptibly();
        verify(lockableGroupStateMachine.readLock).acquireUninterruptibly(10);

        reset(lockableGroupStateMachine.writeLock);
        reset(lockableGroupStateMachine.readLock);

        lease = lockableGroupStateMachine.lockForWriteWithResources(initializer);
        assertNotNull(lease);
        verify(lockableGroupStateMachine.writeLock).acquireUninterruptibly();
        verify(lockableGroupStateMachine.readLock).acquireUninterruptibly(10);
    }

    @Test
    public void testUnlockPersistent() {
        lockableGroupStateMachine.unlockPersistent();
        verify(lockableGroupStateMachine.readLock).release(10);
        verify(lockableGroupStateMachine.writeLock).release();
    }

    @Test
    public void testIsDirty() {
        assertFalse(lockableGroupStateMachine.isDirty());
        lockableGroupStateMachine.setDirty(true);
        assertTrue(lockableGroupStateMachine.isDirty());
        lockableGroupStateMachine.setDirty(false);
        assertFalse(lockableGroupStateMachine.isDirty());
    }

}