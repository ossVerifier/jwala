package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Serializable, Audited {

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

    public abstract Long getId();

    @PrePersist
    private void prePersist() {
        final Calendar now = Calendar.getInstance();
        setCreateDate(now);
        setCreateBy(User.getThreadLocalUser().getId());
        setLastUpdateDate(now);
        setUpdateBy(User.getThreadLocalUser().getId());
    }

    @PreUpdate
    private void preUpdate() {
        final Calendar now = Calendar.getInstance();
        setLastUpdateDate(now);
        setUpdateBy(User.getThreadLocalUser().getId());
    }
}
