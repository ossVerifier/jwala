package com.siemens.cto.aem.domain.model.state;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

public class CurrentState<S extends Object, T extends OperationalState> implements KeyValueStateProvider {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();
    public static final String DEFAULT_EMPTY_MESSAGE = "";

    private final Identifier<S> id;
    private final T state;
    private final DateTime asOf;
    private final StateType type;
    private final String message;

    public CurrentState(final Identifier<S> theId,
                        final T theState,
                        final DateTime theAsOf,
                        final StateType theStateType) {
        this(theId,
             theState,
             theAsOf,
             theStateType,
             DEFAULT_EMPTY_MESSAGE);
    }

    public CurrentState(final Identifier<S> theId,
                        final T theState,
                        final DateTime theAsOf,
                        final StateType theStateType,
                        final String theMessage) {
        id = theId;
        state = theState;
        asOf = theAsOf;
        type = theStateType;
        message = theMessage;
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

    public boolean isTransientState() {
        return state.getTransience().isTransient();
    }

    public boolean isStableState() {
        return state.getStability().isStable();
    }

    public boolean hasMessage() {
        return (message != null) && (!"".equals(message.trim()));
    }

    public DateTime getAsOf() {
        return asOf;
    }

    public StateType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void provideState(final KeyValueStateConsumer aConsumer) {
        aConsumer.set(CommonStateKey.ID, id.getId().toString());
        aConsumer.set(CommonStateKey.TYPE, type.toString());
        aConsumer.set(CommonStateKey.AS_OF, DATE_TIME_FORMATTER.print(asOf));
        aConsumer.set(CommonStateKey.STATE, state.toPersistentString());
        aConsumer.set(CommonStateKey.MESSAGE, message);
    }

    @SuppressWarnings("unchecked")
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
        CurrentState<S,T> rhs = (CurrentState<S,T>) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .append(this.type, rhs.type)
                .append(this.message, rhs.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(state)
                .append(asOf)
                .append(type)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .append("type", type)
                .append("message", message) // Log the entire, because nobody else will report this message. 
                .toString();
    }
}
