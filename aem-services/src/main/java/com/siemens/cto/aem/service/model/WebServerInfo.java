package com.siemens.cto.aem.service.model;

/**
 * Serializes as: {id:##, name:'', h:'', port:##, groupInfo: { id:##, name:''} } 
 *
 */
public class WebServerInfo {

    private final Long id;
    private final String name;
    private final String host;
    private final Integer port;
    private final GroupInfo groupInfo;

    public WebServerInfo(Long id, String name, String host, Integer port, GroupInfo groupInfo) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.groupInfo = groupInfo;
        this.port = port;
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

	public Integer getPort() {
		return port;
	}

}