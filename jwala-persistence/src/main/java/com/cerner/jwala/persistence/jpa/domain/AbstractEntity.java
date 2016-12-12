package com.cerner.jwala.persistence.jpa.domain;

import javax.persistence.*;

import com.cerner.jwala.common.domain.model.user.User;

import java.io.Serializable;
import java.util.Calendar;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Serializable, Audited {

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
    protected void prePersist() {
        final Calendar now = Calendar.getInstance();
        setCreateDate(now);
        // TODO: Determine why User.getThreadLocalUser is sometimes null. One confirmed case is when Spring Integration spuns the thread.
        // Note: We need the code below to prevent nullpointer exception.
        final String userId = User.getThreadLocalUser() == null ? createBy : User.getThreadLocalUser().getId();
        setCreateBy(userId);
        setLastUpdateDate(now);
        setUpdateBy(userId);
    }

    @PreUpdate
    private void preUpdate() {
        final Calendar now = Calendar.getInstance();
        setLastUpdateDate(now);
        // TODO: Determine why User.getThreadLocalUser is sometimes null. One confirmed case is when Spring Integration spuns the thread.
        // Note: We need the tertiary operation to prevent nullpointer exception.
        setUpdateBy(User.getThreadLocalUser() == null ? createBy : User.getThreadLocalUser().getId());
    }
}
