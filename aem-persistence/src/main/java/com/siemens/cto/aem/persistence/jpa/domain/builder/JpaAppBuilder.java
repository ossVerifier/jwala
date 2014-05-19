package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;

public class JpaAppBuilder {

    public static Application appFrom(JpaApplication jpaApp) {
        
        Application app = new Application();        
        
//        try {
            if(jpaApp.getGroup() != null) {
                JpaGroupBuilder groupBuilder = new JpaGroupBuilder(jpaApp.getGroup());
                app.setGroup(groupBuilder.build());                
            }
            app.setName(jpaApp.getName());
            app.setWarPath(jpaApp.getWarPath());
            app.setWebAppContext(jpaApp.getWebAppContext());
            app.setId(Identifier.id(jpaApp.id, Application.class));
/*        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InternalErrorException(AemFaultType.OBJECT_CONSTRUCTION_FAILURE, "Bean reflection failed", e);
        }*/
        return app;
    }
}
