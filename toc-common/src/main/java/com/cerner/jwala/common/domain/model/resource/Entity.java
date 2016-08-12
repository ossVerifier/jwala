package com.cerner.jwala.common.domain.model.resource;

/**
 * Resource entity that wraps type, group and target.
 * <p/>
 * Created by JC043760 on 3/30/2016.
 */
public class Entity {

    private String type;
    private String group;
    private String target;
    private String parentName;
    private boolean deployToJvms = true;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public boolean getDeployToJvms() {
        return deployToJvms;
    }

    public void setDeployToJvms(boolean deployToJvms){
        this.deployToJvms = deployToJvms;
    }

}
