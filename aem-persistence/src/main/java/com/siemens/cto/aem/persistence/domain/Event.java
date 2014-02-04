package com.siemens.cto.aem.persistence.domain;

import java.util.Calendar;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Event extends AbstractEntity<Event> {

    private static final long serialVersionUID = 1094257938065363627L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Calendar startDate;
    public String state;
    public String name;
    public String level;
    public String callbackUrl;
    public String client;
    public Long jobId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Column(name = "environmentId")
    public Environment environment;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLevel() {
        if ("All".equals(level)) {
            level = null;
        }
        return level;
    }

    public void setLevel(final String level) {
        this.level = level;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(final Calendar startDate) {
        this.startDate = startDate;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getClient() {
        return client;
    }

    public void setClient(final String client) {
        this.client = client;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(final Long jobId) {
        this.jobId = jobId;
    }
}
