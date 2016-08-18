package com.cerner.jwala.common.domain.model.webserver.message;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;

import java.io.Serializable;

public class WebServerHistoryEvent implements Serializable {
    private final Identifier<WebServer> id;
    private final String message;
    private final String userId;
    private final DateTime asOf;
    private final StateType type;
    private final String stateString;

    public WebServerHistoryEvent(Identifier<WebServer> id, String eventType, String userId, String message) {

        this.id = id;
        this.message = message;
        this.userId = userId;
        this.asOf = DateTime.now();
        this.stateString = eventType;
        this.type = StateType.WEB_SERVER;
    }

    public WebServerHistoryEvent(Identifier<WebServer> id, String eventDescription, String userId, DateTime now, WebServerControlOperation operation) {

        this.id = id;
        this.message = eventDescription;
        this.userId = userId;
        this.asOf = now;
        this.stateString = operation.getExternalValue();
        this.type = StateType.WEB_SERVER;
    }

    public Identifier<WebServer> getId() {
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

    public String getStateString() {
        return stateString;
    }

    public StateType getType() {
        return type;
    }
}
