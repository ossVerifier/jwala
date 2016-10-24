package com.cerner.jwala.common.domain.model.jvm.message;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.state.StateType;

import java.io.Serializable;

public class JvmHistoryEvent implements Serializable {
    private final Identifier<Jvm> id;
    private final String message;
    private final String userId;
    private final DateTime asOf;
    private final StateType type;
    private final String stateString;

    public JvmHistoryEvent(Identifier<Jvm> id, String eventDescription, String userId, DateTime now, JvmControlOperation operation) {
        this(id, eventDescription, userId, now, operation.getExternalValue());
    }

    public JvmHistoryEvent(Identifier<Jvm> id, String message, String userId, DateTime asOf, String stateString) {
        this.id = id;
        this.message = message;
        this.userId = userId;
        this.asOf = asOf;
        this.type = StateType.JVM;
        this.stateString = stateString;
    }

    public Identifier<Jvm> getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public DateTime getAsOf() {
        return asOf;
    }

    public StateType getType() {
        return type;
    }

    public String getStateString() {
        return stateString;
    }
}
