package com.siemens.cto.aem.domain.model.jvm.message;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;

public class JvmStateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    private final String id;
    private final String instanceId;
    private final String type;
    private final String state;
    private final String asOf;

    public JvmStateMessage(final String theId,
                           final String theInstanceId,
                           final String theType,
                           final String theState,
                           final String theAsOf) {
        id = theId;
        instanceId = theInstanceId;
        type = theType;
        state = theState;
        asOf = theAsOf;
    }

    public String getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getAsOf() {
        return asOf;
    }

    public SetJvmStateCommand toCommand() {
        return new SetJvmStateCommand(getCurrentJvmState());
    }

    protected CurrentJvmState getCurrentJvmState() {
        return new CurrentJvmState(new Identifier<Jvm>(id),
                                   JvmState.valueOf(state),
                                   createAsOf());
    }

    protected DateTime createAsOf() {
        return DATE_TIME_FORMATTER.parseDateTime(asOf);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        JvmStateMessage rhs = (JvmStateMessage) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.instanceId, rhs.instanceId)
                .append(this.type, rhs.type)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(instanceId)
                .append(type)
                .append(state)
                .append(asOf)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("instanceId", instanceId)
                .append("type", type)
                .append("state", state)
                .append("asOf", asOf)
                .toString();
    }
}
