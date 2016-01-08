package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static com.siemens.cto.aem.common.domain.model.group.GroupState.*;
import static com.siemens.cto.aem.common.domain.model.jvm.JvmState.*;
import static com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState.*;

/**
 * A Finite State Machine (FSM) for computing group states.
 *
 * Created by JC043760 on 1/4/2016.
 */
public class GroupFiniteStateMachine {

    private static final Map<Identifier<Group>, GroupState> groupStateMap = new HashMap<>();
    private static final Map<GroupState, Node> FSM = new ConcurrentHashMap<>();

    static {
        configureFsm();
    }

    private static class InstanceHolder {
        static final GroupFiniteStateMachine INSTANCE = new GroupFiniteStateMachine();
    }

    private GroupFiniteStateMachine() {}

    public static GroupFiniteStateMachine getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Node class.
     */
    private static class Node {
        final Map<OperationalState, GroupState> edges;

        public Node(Map<OperationalState, GroupState> edges) {
            this.edges = edges;
        }

        public GroupState transit(OperationalState sig) {
            return edges.get(sig);
        }
    }

    /**
     * Node builder class.
     */
    private static class NodeBuilder {
        private Map<OperationalState, GroupState> edges = new ConcurrentHashMap<>();

        public NodeBuilder() {
        }

        public NodeBuilder edge(OperationalState signal, GroupState endpoint) {
            edges.put(signal, endpoint);
            return this;
        }

        public NodeBuilder edges(Set<OperationalState> transitions, GroupState endpoint) {
            for (OperationalState state : transitions) {
                edges.put(state, endpoint);
            }
            return this;
        }

        public Node build() {
            return new Node(edges);
        }
    }

    /**
     * Creates temporary buckets of similar states to simplify the initialization of the state machine.
     */
    private static void configureFsm() {
        // Starting states
        Set<OperationalState> startingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{JVM_NEW,
                                                             JVM_INITIALIZING,
                                                             JVM_INITIALIZED,
                                                             JVM_START,
                                                             JVM_STARTING,
                                                             JVM_STARTED,
                                                             WS_REACHABLE,
                                                             WS_START_SENT}) {
            startingStates.add(state);
        }

        // Stopping states
        Set<OperationalState> stoppingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{JVM_STOP,
                                                             JVM_STOPPING,
                                                             JVM_STOPPED,
                                                             JVM_DESTROYING,
                                                             JVM_DESTROYED,
                                                             SVC_STOPPED,
                                                             WS_UNREACHABLE,
                                                             WS_STOP_SENT}) {
            stoppingStates.add(state);
        }

        // Failing states
        Set<OperationalState> failingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{JVM_FAILED,
                                                             WS_FAILED}) {
            failingStates.add(state);
        }

        // Unknown states
        Set<OperationalState> unknownStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{JVM_UNKNOWN, /* was reused for group state ? */
                                                             JVM_UNKNOWN,
                                                             WS_UNKNOWN}) {
            unknownStates.add(state);
        }

        // Begin state machine initialization
        FSM.put(GRP_UNKNOWN, new NodeBuilder().edges(startingStates, GRP_STARTING)
                                              .edges(stoppingStates, GRP_STOPPING)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_UNKNOWN)
                                              .edge(JVM_STARTED, GRP_STARTED)
                                              .edge(WS_REACHABLE, GRP_STARTED)
                                              .edge(SVC_STOPPED, GRP_STOPPED)
                                              .edge(WS_UNREACHABLE, GRP_STOPPED)
                                              .build());

        FSM.put(GRP_INITIALIZED, FSM.get(GRP_UNKNOWN));

        FSM.put(GRP_STARTING, new NodeBuilder().edges(startingStates, GRP_STARTING)
                                               .edges(stoppingStates, GRP_PARTIAL)
                                               .edges(failingStates, GRP_FAILURE)
                                               .edges(unknownStates, GRP_PARTIAL)
                                               .build());

        FSM.put(GRP_STOPPING, new NodeBuilder().edges(startingStates, GRP_PARTIAL)
                                               .edges(stoppingStates, GRP_STOPPING)
                                               .edges(failingStates, GRP_FAILURE)
                                               .edges(unknownStates, GRP_PARTIAL)
                                               .build());

        FSM.put(GRP_STARTED, new NodeBuilder().edges(startingStates, GRP_STARTING)
                                              .edges(stoppingStates, GRP_PARTIAL)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_PARTIAL)
                                              .edge(JVM_STARTED, GRP_STARTED)
                                              .edge(WS_REACHABLE, GRP_STARTED)
                                              .build());

        FSM.put(GRP_STOPPED, new NodeBuilder().edges(startingStates, GRP_PARTIAL)
                                              .edges(stoppingStates, GRP_STOPPING)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_PARTIAL)
                                              .edge(SVC_STOPPED, GRP_STOPPED)
                                              .edge(WS_UNREACHABLE, GRP_STOPPED)
                                              .build());

        FSM.put(GRP_PARTIAL, new NodeBuilder().edges(startingStates, GRP_PARTIAL)
                                              .edges(stoppingStates, GRP_PARTIAL)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_PARTIAL)
                                              .build());

        FSM.put(GRP_FAILURE, new NodeBuilder().edges(startingStates, GRP_FAILURE)
                                              .edges(stoppingStates, GRP_FAILURE)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_FAILURE)
                                              .build());
    }

    /**
     * Compute for the group state based on the current group state specified by the groupState parameter
     * and the web server or JVM's current operational state.
     * @param groupState the current group state.
     * @param state the web server/jvm current state.
     * @return the new group state.
     */
    public GroupState computeGroupState(final GroupState groupState, final OperationalState state) {
        return FSM.get(groupState).transit(state);
    }

}
