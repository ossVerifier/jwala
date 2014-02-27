package com.siemens.cto.aem.service.model;

public class JvmInfo {

    private final Long id;
    private final String name;
    private final String host;
    private final GroupInfo groupInfo;

    public JvmInfo(Long id, String name, String host, GroupInfo groupInfo) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.groupInfo = groupInfo;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

}