package com.siemens.cto.aem.persistence.domain;

public enum ServerStateEnum {

    NEW("NEW"), INSTALLING_AGENT("INSTALLING_AGENT"), INSTALLING_ERROR("INSTALLING_ERROR"), RE_INSTALLING_AGENT(
            "RE_INSTALLING_AGENT"), RE_INSTALLING_ERROR("RE_INSTALLING_ERROR"), STARTING("STARTING"), VALIDATING(
            "VALIDATING"), SERVER_VALIDATED("SERVER_VALIDATED"), SERVER_POST_VALIDATED("SERVER_POST_VALIDATED"), INITIALIZED_AGENT(
            "INITIALIZED_AGENT"), EXECUTING("EXECUTING"), LEVEL_COMPLETE("LEVEL_COMPLETE"), REBOOTING("REBOOTING"), COMPLETE(
            "COMPLETE"), PAUSED("PAUSED"), PAUSING("PAUSING"), PAUSE_AFTER_REBOOT("PAUSE_AFTER_REBOOT"), ERROR("ERROR"), FINALIZING(
            "FINALIZING"), FINALIZING_ERROR("FINALIZING_ERROR"), RESET("RESET"), FINALIZED("FINALIZED");

    private String description;

    private ServerStateEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ServerStateEnum fromString(String text) {
        if (text != null) {
            for (ServerStateEnum serverStateEnum : ServerStateEnum.values()) {
                if (text.equalsIgnoreCase(serverStateEnum.name())) {
                    return serverStateEnum;
                }
            }
        }
        return null;
    }
}
