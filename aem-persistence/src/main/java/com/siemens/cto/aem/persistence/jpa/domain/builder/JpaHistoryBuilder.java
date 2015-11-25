package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.group.History;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;

public class JpaHistoryBuilder {

    private JpaHistory jpaHistory;

    public JpaHistoryBuilder(JpaHistory jpaHistory) {
        this.jpaHistory = jpaHistory;
    }

    public History build() {
        return new History(new Identifier<History>(jpaHistory.getId()), jpaHistory.getHistory());
    }
}
