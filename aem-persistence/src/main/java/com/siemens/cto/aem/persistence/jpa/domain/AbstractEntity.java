package com.siemens.cto.aem.persistence.jpa.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.siemens.cto.aem.domain.model.id.Identifier;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T, POJO>, POJO> implements Serializable, Audited {

    private static final long serialVersionUID = 5211000020477780062L;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createDate")
    public Calendar createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastUpdateDate")
    public Calendar lastUpdateDate;

    @Column(name = "createBy")
    public String createBy;

    @Column(name = "updateBy")
    public String updateBy;

    @Column(nullable = false, unique = true)
    public String name;

    @Override
    public Calendar getCreateDate() {
        return createDate;
    }

    @Override
    public void setCreateDate(final Calendar createDate) {
        this.createDate = createDate;
    }

    @Override
    public Calendar getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public void setLastUpdateDate(final Calendar lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(final String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(final String updateBy) {
        this.updateBy = updateBy;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
    
    public abstract Long getId();
    
    public Identifier<POJO> id() {
        return Identifier.<POJO>id(this.getId());
    }
}
