package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;

public class JpaAppBuilder {

    public static Application appFrom(JpaApplication jpaApp) {

        return new Application(
                Identifier.<Application>id(jpaApp.getId()),
                jpaApp.getName(), 
                jpaApp.getWarPath(), 
                jpaApp.getWebAppContext(), 
                jpaApp.getGroup() != null ? new JpaGroupBuilder(jpaApp.getGroup()).build() : null,
                jpaApp.isSecure(),
                jpaApp.isLoadBalanceAcrossServers(),
                jpaApp.isUnpackWar(),
                jpaApp.getWarName());

    }
}
