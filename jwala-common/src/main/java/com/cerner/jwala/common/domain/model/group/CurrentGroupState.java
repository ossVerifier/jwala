package com.cerner.jwala.common.domain.model.group;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.KeyValueStateConsumer;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.state.message.CommonStateKey;

public class CurrentGroupState extends CurrentState<Group, GroupState> {

    public static class StateDetail {

        private int started;
        private int total;

        public StateDetail(final int started, final int total) {
            this.started = started;
            this.total = total;
        }
        public void setStarted(int started) {
            this.started = started;
        }
        public int getStarted() {
            return this.started;
        }
        public void setTotal(int total) {
            this.total = total;
        }
        public int getTotal() {
            return this.total;
        }
        public float getPercentStarted() {
            return this.total == 0 ? 1.0f : ((float)started) / total;
        }
        @Override // webservers: {started: 1, total: 2}
        public String toString() { return "{started: " + started + ", total: "+ total + "}"; }
    }

    private final StateDetail webServers;
    private final StateDetail jvms;

    public CurrentGroupState(Identifier<Group> theId, GroupState theState, DateTime theAsOf, StateDetail jvmsDetail, StateDetail webServersDetail ) {
        super(theId, theState, theAsOf, StateType.GROUP);
        this.jvms = jvmsDetail;
        this.webServers = webServersDetail;
    }

    public CurrentGroupState(Identifier<Group> theId, GroupState theState, DateTime theAsOf) {
        this(theId, theState, theAsOf,new StateDetail(0,0),new StateDetail(0,0));
    }

    public StateDetail getWebServersDetail() {
        return this.webServers;
    }

    public StateDetail getJvmsDetail() {
        return this.jvms;
    }

    @Override
    public String toString() {
        return super.toString() + ", 'detail': {jvms: "+jvms +", webservers: " + webServers + "}";
    }
}
