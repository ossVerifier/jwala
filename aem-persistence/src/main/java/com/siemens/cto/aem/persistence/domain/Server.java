package com.siemens.cto.aem.persistence.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Server extends AbstractEntity<Server> {

    private static final long serialVersionUID = 8265197266890650695L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    private String hostName;
    private String profile;

    @ManyToOne
    @JoinColumn(name = "environmentId")
    private Environment environment;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "server")
    private ServerExecuteState serverExecuteState;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(final String profile) {
        this.profile = profile;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public boolean isRunning() {
        return !notRunning();
    }

    public boolean notRunning() {
        return ServerStateEnum.COMPLETE.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.ERROR.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.NEW.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.PAUSED.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.PAUSE_AFTER_REBOOT.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.RESET.equals(getServerExecuteState().getServerStateEnum())
                || ServerStateEnum.FINALIZED.equals(getServerExecuteState().getServerStateEnum());
    }

    public ServerExecuteState getServerExecuteState() {
        return serverExecuteState;
    }

    public void setServerExecuteState(final ServerExecuteState serverExecuteState) {
        this.serverExecuteState = serverExecuteState;
    }

    /**
     * Overriding this to avoid dirtying up the log file with all states
     */
    @Override
    public String toString() {
        final StringBuilder toString = new StringBuilder();
        toString.append("Server[");
        toString.append("id=").append(id);
        toString.append(", hostName='").append(hostName).append('\'');
        toString.append(", profile='").append(profile).append('\'');
        toString.append(", serverExecuteState=").append(serverExecuteState);
        toString.append(']');
        return toString.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Server server = (Server) o;

        return !(id != null ? !id.equals(server.id) : server.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isComplete() {
        return ServerStateEnum.COMPLETE.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isInstallingError() {
        return ServerStateEnum.INSTALLING_ERROR.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isError() {
        return ServerStateEnum.ERROR.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isRebooting() {
        return ServerStateEnum.REBOOTING.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isPaused() {
        return ServerStateEnum.PAUSED.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isNew() {
        return ServerStateEnum.NEW.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isLevelComplete() {
        return ServerStateEnum.LEVEL_COMPLETE.equals(getServerExecuteState().getServerStateEnum());
    }

    public boolean isFinalized() {
        return ServerStateEnum.FINALIZED.equals(getServerExecuteState().getServerStateEnum());
    }
}
