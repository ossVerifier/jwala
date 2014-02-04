package com.siemens.cto.aem.persistence.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "server_execute_state")
public class ServerExecuteState extends AbstractEntity<ServerExecuteState> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 1000)
    private String message;
    private String percentComplete;
    private String targetLevel;
    private String lastLevel;
    private String nextLevel;

    @Column(length = 1000)
    private String installState;

    @Enumerated(EnumType.STRING)
    @Column(name = "serverState")
    private ServerStateEnum serverStateEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "startType")
    private StartTypeEnum startTypeEnum;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "serverId")
    private Server server;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(final String percentComplete) {
        this.percentComplete = percentComplete;
    }

    public ServerStateEnum getServerStateEnum() {
        return serverStateEnum;
    }

    public void setServerStateEnum(final ServerStateEnum serverStateEnum) {
        this.serverStateEnum = serverStateEnum;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(final String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getLastLevel() {
        return lastLevel;
    }

    public void setLastLevel(final String lastLevel) {
        this.lastLevel = lastLevel;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(final Server server) {
        this.server = server;
    }

    public StartTypeEnum getStartTypeEnum() {
        return startTypeEnum;
    }

    public void setStartTypeEnum(final StartTypeEnum startTypeEnum) {
        this.startTypeEnum = startTypeEnum;
    }

    public String getNextLevel() {
        return nextLevel;
    }

    public void setNextLevel(final String nextLevel) {
        this.nextLevel = nextLevel;
    }

    public String getInstallState() {
        return installState;
    }

    public void setInstallState(final String installState) {
        this.installState = installState;
    }

    @Override
    public String toString() {
        return "ServerExecuteState{" + "serverStateEnum=" + serverStateEnum + ", id=" + id + ", message='" + message
                + '\'' + ", percentComplete='" + percentComplete + '\'' + ", targetLevel='" + targetLevel + '\''
                + ", lastLevel='" + lastLevel + '\'' + ", nextLevel='" + nextLevel + '\'' + ", startTypeEnum='"
                + startTypeEnum + '\'' + ", installState='" + installState + '\'' + '}';
    }
}
