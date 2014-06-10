package com.siemens.cto.aem.domain.model.jvm.message;

import java.io.Serializable;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JvmStateMessage that = (JvmStateMessage) o;

        if (asOf != null ? !asOf.equals(that.asOf) : that.asOf != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (asOf != null ? asOf.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JvmStateMessage{" +
               "id='" + id + '\'' +
               ", instanceId='" + instanceId + '\'' +
               ", type='" + type + '\'' +
               ", state='" + state + '\'' +
               ", asOf='" + asOf + '\'' +
               '}';
    }
}
