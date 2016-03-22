package com.siemens.cto.aem.common.domain.model.state;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.message.CommonStateKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class CurrentState<S, T extends OperationalState> implements KeyValueStateProvider {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();
    public static final String DEFAULT_EMPTY_MESSAGE = "";

    private final Identifier<S> id;
    private final T state;
    private final DateTime asOf;
    private final StateType type;
    private final String message;
    private String userId; // TODO: Have this set in the constructor.

    private Long webServerCount;
    private Long webServerStartedCount;
    private Long jvmCount;
    private Long jvmStartedCount;

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType stateType) {
        this(id, state, asOf, stateType, DEFAULT_EMPTY_MESSAGE);
    }

    public CurrentState(final Identifier<S> id, final T state, final String userId, final DateTime asOf, final StateType stateType) {
        this(id, state, asOf, stateType, DEFAULT_EMPTY_MESSAGE);
        setUserId(userId);
    }

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type,
                        final Long webServerCount, final Long webServerStartedCount, final Long jvmCount,
                        final Long jvmStartedCount) {
        this.id = id;
        this.state = state;
        this.asOf = asOf;
        this.type = type;
        this.message = DEFAULT_EMPTY_MESSAGE;
        this.webServerCount = webServerCount;
        this.webServerStartedCount = webServerStartedCount;
        this.jvmCount = jvmCount;
        this.jvmStartedCount = jvmStartedCount;
    }

    public CurrentState(final Identifier<S> id, final T state, final DateTime asOf, final StateType type, final String message) {
        this.id = id;
        this.state = state;
        this.asOf = asOf;
        this.type = type;
        this.message = message;
    }

    public Identifier<S> getId() {
        return id;
    }

    public T getState() {
        return state;
    }

    public String getStateString() {
        return state.toStateLabel();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void provideState(final KeyValueStateConsumer aConsumer) {
        aConsumer.set(CommonStateKey.ID, id.getId().toString());
        aConsumer.set(CommonStateKey.TYPE, type.toString());
        aConsumer.set(CommonStateKey.AS_OF, DATE_TIME_FORMATTER.print(asOf));
        aConsumer.set(CommonStateKey.STATE, state.toPersistentString());
        aConsumer.set(CommonStateKey.MESSAGE, message);
        aConsumer.set(CommonStateKey.USERID, userId);
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
                .append(this.userId, rhs.userId)
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
                .append(userId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .append("type", type)
                .append("message", message)
                .append("userId", userId) // Log the entire, because nobody else will report this message.
                .toString();
    }

    public Long getWebServerCount() {
        return webServerCount;
    }

    public Long getWebServerStartedCount() {
        return webServerStartedCount;
    }

    public Long getJvmCount() {
        return jvmCount;
    }

    public Long getJvmStartedCount() {
        return jvmStartedCount;
    }

}
