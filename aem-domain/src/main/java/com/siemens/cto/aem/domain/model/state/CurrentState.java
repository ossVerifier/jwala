package com.siemens.cto.aem.domain.model.state;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

public class CurrentState<S extends Object, T extends ExternalizableState> implements KeyValueStateProvider {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    private final Identifier<S> id;
    private final T state;
    private final DateTime asOf;
    private final StateType type;

    public CurrentState(final Identifier<S> theId,
                        final T theState,
                        final DateTime theAsOf,
                        final StateType theStateType) {
        id = theId;
        state = theState;
        asOf = theAsOf;
        type = theStateType;
    }

    public Identifier<S> getId() {
        return id;
    }

    public T getState() {
        return state;
    }

    public String getStateString() {
        return state.toStateString();
    }

    public DateTime getAsOf() {
        return asOf;
    }

    public StateType getType() {
        return type;
    }

    @Override
    public void provideState(final KeyValueStateConsumer aConsumer) {
        aConsumer.set(CommonStateKey.ID, id.getId().toString());
        aConsumer.set(CommonStateKey.TYPE, type.toString());
        aConsumer.set(CommonStateKey.AS_OF, DATE_TIME_FORMATTER.print(asOf));
        aConsumer.set(CommonStateKey.STATE, state.toStateString());
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
        CurrentState rhs = (CurrentState) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .append(this.type, rhs.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(state)
                .append(asOf)
                .append(type)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .append("type", type)
                .toString();
    }
}
