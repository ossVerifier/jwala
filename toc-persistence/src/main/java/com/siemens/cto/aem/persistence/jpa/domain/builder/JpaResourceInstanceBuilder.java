package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;

/**
 * Created by z003e5zv on 3/20/2015.
 */
public class JpaResourceInstanceBuilder  {

    private JpaResourceInstance jpaResourceInstance;

    public JpaResourceInstanceBuilder(JpaResourceInstance jpaResourceInstance) {
        this.jpaResourceInstance = jpaResourceInstance;
    }

    public ResourceInstance build() {
        JpaLiteGroupBuilder jpaGroupBuilder = new JpaLiteGroupBuilder(jpaResourceInstance.getGroup());
        return new ResourceInstance(
                new Identifier<ResourceInstance>(jpaResourceInstance.getId()),
                jpaResourceInstance.getName(),
                jpaResourceInstance.getResourceTypeName(),
                jpaGroupBuilder.build(),
                jpaResourceInstance.getAttributes()
        );
    }

}
