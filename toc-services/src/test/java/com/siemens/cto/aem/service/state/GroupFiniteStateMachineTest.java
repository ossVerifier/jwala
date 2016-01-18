package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.domain.model.group.GroupState;
import org.junit.Test;

import static com.siemens.cto.aem.common.domain.model.group.GroupState.*;
import static com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState.*;
import static com.siemens.cto.aem.common.domain.model.jvm.JvmState.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link GroupFiniteStateMachine}.
 *
 * Created by JC043760 on 1/18/2016.
 */
public class GroupFiniteStateMachineTest {

    @Test
    // Tests common group state transition scenarios for web server states
    // TODO: Test all possible test cases not just the common ones.
    public void testComputeGroupStatesForWebServerStates() {
        GroupState state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, WS_REACHABLE);
        assertEquals(GRP_STARTED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, WS_UNREACHABLE);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, WS_REACHABLE);
        assertEquals(GRP_STARTED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, WS_UNREACHABLE);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STOPPED, WS_UNREACHABLE);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STOPPED, WS_REACHABLE);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTING, WS_REACHABLE);
        assertEquals(GRP_STARTING, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTING, WS_UNREACHABLE);
        assertEquals(GRP_PARTIAL, state);
    }

    @Test
    // Tests common group state transition scenarios for JVM states
    // TODO: Test all possible test cases not just the common ones.
    public void testComputeGroupStatesForJvmStates() {
        GroupState state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, JVM_STARTED);
        assertEquals(GRP_STARTED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, JVM_STOPPED);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, SVC_STOPPED);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, JVM_INITIALIZED);
        assertEquals(GRP_STARTING, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, JVM_STARTED);
        assertEquals(GRP_STARTED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, JVM_STOPPED);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTED, SVC_STOPPED);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTING, JVM_STARTED);
        assertEquals(GRP_STARTING, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTING, JVM_STOPPED);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STARTING, SVC_STOPPED);
        assertEquals(GRP_PARTIAL, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_UNKNOWN, JVM_STOP);
        assertEquals(GRP_STOPPING, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STOPPED, JVM_STOPPED);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STOPPED, SVC_STOPPED);
        assertEquals(GRP_STOPPED, state);

        state = GroupFiniteStateMachine.getInstance().computeGroupState(GRP_STOPPED, JVM_STARTED);
        assertEquals(GRP_PARTIAL, state);
    }

}
