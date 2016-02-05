package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;

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
        final Set<OperationalState> startingStates = new TreeSet<>(new OperationalStateComparator());
        startingStates.add(JVM_NEW);
        startingStates.add(JVM_INITIALIZING);
        startingStates.add(JVM_INITIALIZED);
        startingStates.add(JVM_START);
        startingStates.add(JVM_STARTING);
        startingStates.add(JVM_STARTED);
        startingStates.add(WS_REACHABLE);
        startingStates.add(WS_START_SENT);

        final Set<OperationalState> stoppingStates = new TreeSet<>(new OperationalStateComparator());
        stoppingStates.add(JVM_STOP);
        stoppingStates.add(JVM_STOPPING);
        stoppingStates.add(JVM_STOPPED);
        stoppingStates.add(JVM_DESTROYING);
        stoppingStates.add(JVM_DESTROYED);
        stoppingStates.add(FORCED_STOPPED);
        stoppingStates.add(WS_UNREACHABLE);
        stoppingStates.add(WS_STOP_SENT);

        // Failing states
        final Set<OperationalState> failingStates = new TreeSet<>(new OperationalStateComparator());
        failingStates.add(JVM_FAILED);
        failingStates.add(WS_FAILED);

        // Unknown states
        final Set<OperationalState> unknownStates = new TreeSet<>(new OperationalStateComparator());
        unknownStates.add(JVM_UNKNOWN);
        unknownStates.add(WS_UNKNOWN);

        // Begin state machine initialization
        FSM.put(GRP_UNKNOWN, new NodeBuilder().edges(startingStates, GRP_STARTING)
                                              .edges(stoppingStates, GRP_STOPPING)
                                              .edges(failingStates, GRP_FAILURE)
                                              .edges(unknownStates, GRP_UNKNOWN)
                                              .edge(JVM_STARTED, GRP_STARTED)
                                              .edge(WS_REACHABLE, GRP_STARTED)
                                              .edge(JVM_STOPPED, GRP_STOPPED)
                                              .edge(FORCED_STOPPED, GRP_STOPPED)
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
                                              .edge(JVM_STOPPED, GRP_STOPPED)
                                              .edge(FORCED_STOPPED, GRP_STOPPED)
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
