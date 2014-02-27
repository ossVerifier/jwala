package com.siemens.cto.aem.service.model;

/**
 * Created by Z003BPEJ on 2/27/14.
 */
public class GroupInfo {

    private final Long id;
    private final String name;

    public GroupInfo(String name) {
        this.id = null;
        this.name = name;
    }

    public GroupInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
