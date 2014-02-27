package com.siemens.cto.aem.service.model;

public class JvmInfo {

    private final Long id;
    private final String name;
    private final String host;

    public JvmInfo(Long id, String name, String host) {
        this.id = id;
        this.name = name;
        this.host = host;
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

}