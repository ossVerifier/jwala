package com.siemens.cto.aem.persistence.jpa.type;

/**
 * List of history event types.
 *
 * Created by JC043760 on 12/9/2015.
 */
public enum EventType {

    USER_ACTION("A"), APPLICATION_ERROR("E"), UNKNOWN(null);

    private final String abbrev;

    EventType(final String abbrev) {
        this.abbrev = abbrev;
    }

    public static EventType fromValue(final String val) {
        for (EventType eventType: values()) {
            if (eventType.abbrev.equalsIgnoreCase(val)) {
                return eventType;
            }
        }
        return UNKNOWN;
    }

    public String toValue() {
        return abbrev;
    }

}
