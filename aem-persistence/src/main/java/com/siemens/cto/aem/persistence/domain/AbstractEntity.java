package com.siemens.cto.aem.persistence.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.siemens.cto.aem.common.Objects;
import com.siemens.cto.aem.common.User;

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

    @Column(nullable = false, unique = true)
    public String name;

    @PrePersist
    public void atCreation() {
        final Calendar cal = Calendar.getInstance();
        this.setCreateDate(cal);
        this.setLastUpdateDate(cal);

        this.setCreateBy(getSessionUser());
        this.setUpdateBy(getSessionUser());
    }

    @PreUpdate
    public void atUpdate() {
        this.setLastUpdateDate(Calendar.getInstance());
        this.setUpdateBy(getSessionUser());
    }

    private static String getSessionUser() {
        return User.getUser().getUserName();
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    public String toString() {
        final StringBuilder builder =
                new StringBuilder(getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1, getClass().getName().length()) + '[');

        final Field[] fields = getClass().getDeclaredFields();

        boolean needComma = false;
        for (final Field f : fields) {
            final String name = f.getName();
            if (name != null && (name.startsWith("pc") || name.startsWith("class$") || name.equals("serialVersionUID"))) {
                continue;
            }
            if (needComma) {
                builder.append(", ");
            }
            f.setAccessible(true);
            builder.append(name);
            builder.append("=");
            try {
                if (f.getType().equals(Calendar.class) && f.get(this) != null) {
                    builder.append(Objects.formatDate(((Calendar) f.get(this)).getTime()));
                } else {
                    builder.append(f.get(this));
                }
                needComma = true;
            } catch (final IllegalAccessException e) {
                // do nothing
            }
        }

        builder.append(']');
        return builder.toString();
    }

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
}
