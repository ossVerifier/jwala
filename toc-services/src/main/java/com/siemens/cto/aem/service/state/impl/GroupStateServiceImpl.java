package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.request.group.ControlGroupRequest;
import com.siemens.cto.aem.common.request.group.SetGroupStateRequest;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.group.impl.LockableGroupStateMachine;
import com.siemens.cto.aem.service.group.impl.LockableGroupStateMachine.Initializer;
import com.siemens.cto.aem.service.group.impl.LockableGroupStateMachine.Lease;
import com.siemens.cto.aem.service.group.impl.LockableGroupStateMachine.ReadWriteLease;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.*;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * Primary Group State Service.
 *
 * 1. Reacts to incoming state changes on the state bus.
 * 2. Recalculates group state for all affected groups.
 * 3. Handles jvm or web server.
 *
 * Recent changes: Now utilizing group configuration
 */
public class GroupStateServiceImpl extends StateServiceImpl<Group, GroupState> implements StateService<Group, GroupState>, GroupStateService.API, ApplicationContextAware {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceImpl.class);
    private static final String GSS_UNLOCK = "GSS Unlock: {}";
    private static final String GSS_UNLOCKED_AFFECTED_GROUPS_DUE_TO_EXCEPTION = "GSS Unlocked affected groups due to exception.";
    private static final String GROUP_STATE_MACHINE = "groupStateMachine";

    private final GroupPersistenceService groupPersistenceService;

    private final JvmPersistenceService jvmPersistenceService;

    private final WebServerPersistenceService webServerCrudService;

    private final Map<Identifier<Group>, LockableGroupStateMachine> allGSMs = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    private final User systemUser;

    public GroupStateServiceImpl(StatePersistenceService<Group, GroupState> thePersistenceService,
                                 StateNotificationService theNotificationService, StateType theStateType,
                                 final GroupPersistenceService groupPersistenceService,
                                 final JvmPersistenceService jvmPersistenceService,
                                 final WebServerPersistenceService webServerPersistenceService,
                                 final GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        super(thePersistenceService, theNotificationService, theStateType,
                grpStateComputationAndNotificationSvc);

        systemUser = User.getSystemUser();

        this.groupPersistenceService = groupPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.webServerCrudService = webServerPersistenceService;
    }


    @Transactional(readOnly = true)
    @Override
    public List<SetGroupStateRequest> stateUpdateJvm(CurrentState<Jvm, JvmState> cjs) throws InterruptedException {

        LOGGER.debug("Recalculating group state due to jvm update: " + cjs.toString());

        // lookup children
        Identifier<Jvm> jvmId = cjs.getId();
        Jvm jvm = jvmPersistenceService.getJvm(jvmId);

        if (jvm == null) {
            return Collections.emptyList();
        }

        Set<Group> groups = jvm.getGroups();

        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }

        return refreshGroups(groups);
    }

    private List<SetGroupStateRequest> refreshGroups(Set<Group> groups) throws InterruptedException {
        List<SetGroupStateRequest> result = new ArrayList<>(groups.size());
        List<ReadWriteLease> lockedGsms = new ArrayList<>(groups.size());

        try {
            for (Group group : groups) {

                // Lite group unfortunately not good enough
                final Identifier<Group> groupId = group.getId();
                Group fullGroup = groupPersistenceService.getGroup(groupId);

                result.add(refreshGroupState(lockedGsms, fullGroup));
            }
        } catch (final RuntimeException re) {
            for (ReadWriteLease gsm : lockedGsms) {
                Group group = gsm.getCurrentGroup();
                if (group != null) {
                    LOGGER.warn(GSS_UNLOCK, group);
                    getLockableGsm(group.getId()).unlockPersistent();
                }
            }
            LOGGER.warn(GSS_UNLOCKED_AFFECTED_GROUPS_DUE_TO_EXCEPTION, re);
            result.clear();
            throw re;
        }
        return result;
    }

    private List<SetGroupStateRequest> refreshCollectionGroups(Collection<Group> groups) throws InterruptedException {
        List<SetGroupStateRequest> result = new ArrayList<>(groups.size());
        List<ReadWriteLease> lockedGsms = new ArrayList<>(groups.size());

        try {
            for (Group group : groups) {

                result.add(refreshGroupState(lockedGsms, group));
            }
        } catch (final RuntimeException re) {
            for (ReadWriteLease gsm : lockedGsms) {
                Group group = gsm.getCurrentGroup();
                if (group != null) {
                    LOGGER.warn(GSS_UNLOCK, group);
                    getLockableGsm(group.getId()).unlockPersistent();
                }
            }
            LOGGER.warn(GSS_UNLOCKED_AFFECTED_GROUPS_DUE_TO_EXCEPTION, re);
            result.clear();
            throw re;
        }
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SetGroupStateRequest> stateUpdateWebServer(CurrentState<WebServer, WebServerReachableState> wsState) throws InterruptedException {
        LOGGER.debug("GSS Recalc group state due to web server update: " + wsState.toString());

        // lookup children
        Identifier<WebServer> wsId = wsState.getId();
        WebServer ws = webServerCrudService.getWebServer(wsId);

        if (ws == null) {
            return Collections.emptyList();
        }

        Collection<Group> groups = ws.getGroups();

        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }

        return refreshCollectionGroups(groups);
    }

    @Transactional(readOnly = true)
    @Override
    public SetGroupStateRequest stateUpdateRequest(Group group) throws InterruptedException {
        LOGGER.debug("GSS Recalc group state by request.");

        List<ReadWriteLease> lockedGsms = new ArrayList<>(1);

        try {
            return refreshGroupState(lockedGsms, group);
        } catch (final RuntimeException re) {
            for (ReadWriteLease gsm : lockedGsms) {
                Group group2 = gsm.getCurrentGroup();
                if (group2 != null) {
                    LOGGER.warn(GSS_UNLOCK, group2);
                    getLockableGsm(group2.getId()).unlockPersistent();
                }
            }
            LOGGER.warn(GSS_UNLOCKED_AFFECTED_GROUPS_DUE_TO_EXCEPTION, re);
            throw re;
        }
    }

    /**
     * Helper used by both WS and JVM update
     *
     * @param lockedGsms a collection of active leases
     * @return null for a null message
     */
    private SetGroupStateRequest refreshGroupState(List<ReadWriteLease> lockedGsms, Group group)
            throws InterruptedException {
        // Serialize access
        LockableGroupStateMachine lockableGsm = this.getLockableGsm(group.getId());
        ReadWriteLease gsm = lockableGsm.tryPersistentLock(new Initializer() {
            @Override
            public GroupStateMachine initializeGroupStateMachine() {
                // injection of prototype gsm - one time initialization complete
                return applicationContext.getBean(GROUP_STATE_MACHINE, GroupStateMachine.class);
            }
        }, 1, TimeUnit.SECONDS);

        // handle timeout condition on lock by skipping
        if (gsm == null) {
            LOGGER.warn("Skipping group due to lock {}", group);
            return null;
        } else {
            lockedGsms.add(gsm);
        }

        // Get state before recalculation
        CurrentState<Group, GroupState> priorState = group.getCurrentState();

        // reset GSM and refresh state with new group information.
        gsm.synchronizedInitializeGroup(group, systemUser);

        // mark state machine as dirty if it has changed
        if (priorState != null &&
                priorState.getState() != gsm.getCurrentState()) {
            lockableGsm.setDirty(true);
        }

        // Construct an update. 
        return new SetGroupStateRequest(gsm.getCurrentStateDetail());
    }

    /**
     * @param groupId group to get a state machine for.
     * @return the state machine
     */
    @SuppressWarnings("unchecked")
    private LockableGroupStateMachine getLockableGsm(final Identifier<Group> groupId) {
        LockableGroupStateMachine tempGsm = new LockableGroupStateMachine();
        LockableGroupStateMachine actualGsm = (LockableGroupStateMachine) ((ConcurrentHashMap) allGSMs).putIfAbsent(groupId, tempGsm);

        if (actualGsm == null) {
            actualGsm = tempGsm;
        }

        return actualGsm;
    }

    /**
     * @param groupId group to get a state machine for.
     * @return the state machine
     */
    private ReadWriteLease leaseWritableGsm(final Identifier<Group> groupId, final User user) {
        LockableGroupStateMachine gsm = getLockableGsm(groupId);

        return gsm.lockForWriteWithResources(new Initializer() {
            @Override
            public GroupStateMachine initializeGroupStateMachine() {
                GroupStateMachine newGsm = applicationContext.getBean(GROUP_STATE_MACHINE, GroupStateMachine.class);
                Group group = groupPersistenceService.getGroup(groupId);
                newGsm.synchronizedInitializeGroup(group, user);
                return newGsm;
            }
        });
    }

    /**
     * @param groupId group to get a state machine for.
     * @return the state machine
     */
    private Lease getGsmWithResources(final Identifier<Group> groupId, final User user) {
        LockableGroupStateMachine gsm = getLockableGsm(groupId);

        return gsm.lockForReadWithResources(new Initializer() {
            @Override
            public GroupStateMachine initializeGroupStateMachine() {
                GroupStateMachine newGsm = applicationContext.getBean(GROUP_STATE_MACHINE, GroupStateMachine.class);
                Group group = groupPersistenceService.getGroup(groupId);
                newGsm.synchronizedInitializeGroup(group, user);
                return newGsm;
            }
        });
    }

    private RuntimeException convert(Exception e) {
        return new RuntimeException(e);
    }

    @Override
    public CurrentGroupState signalReset(Identifier<Group> groupId, User user) {
        try (Lease lease = leaseWritableGsm(groupId, user)) {
            return lease.signalReset(user);
        } catch (Exception e) {
            throw convert(e);
        }
    }

    @Override
    public CurrentGroupState signalStopRequested(Identifier<Group> groupId, User user) {
        try (Lease lease = leaseWritableGsm(groupId, user)) {
            return lease.signalStopRequested(user);
        } catch (Exception e) {
            throw convert(e);
        }
    }

    @Override
    public CurrentGroupState signalStartRequested(Identifier<Group> groupId, User user) {
        try (Lease lease = leaseWritableGsm(groupId, user)) {
            return lease.signalStartRequested(user);
        } catch (Exception e) {
            throw convert(e);
        }
    }

    @Override
    public boolean canStart(Identifier<Group> groupId, User user) {
        try (Lease lease = getGsmWithResources(groupId, user).readOnly()) {
            return lease.canStart();
        } catch (Exception e) {
            throw convert(e);
        }
    }

    @Override
    public boolean canStop(Identifier<Group> groupId, User user) {
        try (Lease lease = getGsmWithResources(groupId, user).readOnly()) {
            return lease.canStop();
        } catch (Exception e) {
            throw convert(e);
        }
    }

    @Override
    protected CurrentState<Group, GroupState> createUnknown(Identifier<Group> anId) {
        return new CurrentGroupState(anId, GroupState.GRP_UNKNOWN, DateTime.now());
    }

    @Override
    public CurrentGroupState signal(ControlGroupRequest controlGroupRequest, User aUser) {
        switch (controlGroupRequest.getControlOperation()) {
            case START:
                return signalStartRequested(controlGroupRequest.getGroupId(), aUser);
            case STOP:
                return signalStopRequested(controlGroupRequest.getGroupId(), aUser);
            default:
                return null;
        }
    }

    @Override
    @Transactional
    public SetGroupStateRequest groupStatePersist(SetGroupStateRequest sgsc) {
        // If an empty list is returned by the splitter, it will be treated as single null item, so check
        try {
            if (sgsc != null && sgsc.getNewState() != null) {
                LOGGER.trace("GSS Persist: {}", sgsc.getNewState());
                groupPersistenceService.updateGroupStatus(sgsc);
            }
        } catch (final RuntimeException re) {
            LOGGER.warn("GSS Unlocking group due to database exception.", re);
            groupStateUnlock(sgsc);
            throw re;
        }
        return sgsc;
    }

    @Override
    public SetGroupStateRequest groupStateNotify(SetGroupStateRequest sgsc) {
        // If an empty list is returned by the splitter, it will be treated as single null item, so check
        if (sgsc != null && sgsc.getNewState() != null) {
            if (getLockableGsm(sgsc.getNewState().getId()).isDirty()) {
                LOGGER.trace("GSS Notify: {}", sgsc.getNewState());
                getNotificationService().notifyStateUpdated(sgsc.getNewState());
            } else {
                LOGGER.trace("GSS Discard Notify (Same State): {}", sgsc.getNewState());
            }
            getLockableGsm(sgsc.getNewState().getId()).setDirty(false);
        }
        return sgsc;
    }

    @Override
    public SetGroupStateRequest groupStateUnlock(SetGroupStateRequest sgsc) {
        // If an empty list is returned by the splitter, it will be treated as single null item, so check
        if (sgsc != null && sgsc.getNewState() != null) {
            LOGGER.trace(GSS_UNLOCK, sgsc.getNewState());
            getLockableGsm(sgsc.getNewState().getId()).unlockPersistent();
        }
        return sgsc;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
