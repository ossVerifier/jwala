package com.cerner.jwala.common.domain.model.resource;

/**
 * Resource property POJO.
 *
 * Created by JC043760 on 5/13/2016.
 */
public class ResourceProperty {
    private String name;
    private boolean required;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
