package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.domain.model.id.Identifier;

import java.io.Serializable;

public class History implements Serializable {

    private final Identifier<History> id;
    private final String history;

    public History(Identifier<History> id, String history) {
        this.id = id;
        this.history = history;
    }

    public Identifier<History> getId() {
        return id;
    }

    public String getHistory() {
        return history;
    }
}
