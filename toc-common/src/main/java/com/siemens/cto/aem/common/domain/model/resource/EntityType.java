package com.siemens.cto.aem.common.domain.model.resource;

/**
 * Enumerates possible resource entity types.
 *
 * Created by JC043760 on 3/30/2016.
 */
public enum EntityType {
    JVM("JVM"), GROUPED_JVMS("GROUPED_JVMS"), WEB_SERVER("WEB_SERVER"), GROUPED_WEBSERVERS("GROUPED_WEBSERVERS"),
    APP("APPLICATION"), GROUPED_APPS("GROUPED_APPS"), UNDEFINED(null);

    final private String entityTypeValue;

    EntityType(final String entityTypeValue) {
        this.entityTypeValue = entityTypeValue;
    }

    public static EntityType fromValue(final String entityTypeValue) {
        for (EntityType entityType: EntityType.values()) {
            if (entityType.entityTypeValue.equalsIgnoreCase(entityTypeValue)) {
                return entityType;
            }
        }
        return UNDEFINED;
    }
}
