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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (deployToJvms != entity.deployToJvms) return false;
        if (type != null ? !type.equals(entity.type) : entity.type != null) return false;
        if (group != null ? !group.equals(entity.group) : entity.group != null) return false;
        if (target != null ? !target.equals(entity.target) : entity.target != null) return false;
        return !(parentName != null ? !parentName.equals(entity.parentName) : entity.parentName != null);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (parentName != null ? parentName.hashCode() : 0);
        result = 31 * result + (deployToJvms ? 1 : 0);
        return result;
    }
}
