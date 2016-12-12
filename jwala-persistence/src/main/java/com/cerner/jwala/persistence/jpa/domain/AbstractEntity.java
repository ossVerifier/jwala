package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.user.User;

import javax.persistence.*;
import java.util.Calendar;

@MappedSuperclass
public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Audited {

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
        setCreateBy(getUserId(createBy));
        setLastUpdateDate(now);
    }

    @PreUpdate
    private void preUpdate() {
        final Calendar now = Calendar.getInstance();
        setLastUpdateDate(now);
        setUpdateBy(getUserId(updateBy));
    }

    /**
     * Gets the user name/ID from thread local
     * @param providedUserId the user id provided by through createBy or updateBy
     * @return the user id
     */
    private String getUserId(final String providedUserId) {
        // Note: User.getThreadLocalUser() will be null when the thread that implicitly calls this was created by another
        //       thread. If the User.getThreadLocalUser() is null then we just use providedUserId. Please note that
        //       create and update by can be null if the a persist or update is caused by an automated event or by
        //       succeeding system events caused by user a action. That the said events can
        //       also pass user id information if needed hence the parameter providedUserId.
        return User.getThreadLocalUser() == null ? providedUserId : User.getThreadLocalUser().getId();
    }

}
