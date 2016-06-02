package com.siemens.cto.aem.common.domain.model.jvm.message;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import org.joda.time.DateTime;

import java.io.Serializable;

public class JvmHistoryEvent implements Serializable {
    private final Identifier<Jvm> id;
    private final String message;
    private final String userId;
    private final DateTime asOf;
    private final StateType type;
    private final String stateString;

    public JvmHistoryEvent(Identifier<Jvm> id, String eventDescription, String userId, DateTime now, JvmControlOperation operation) {

        this.id = id;
        this.message = eventDescription;
        this.userId = userId;
        this.asOf = now;
        this.type = StateType.JVM;
        this.stateString = operation.getExternalValue();
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
