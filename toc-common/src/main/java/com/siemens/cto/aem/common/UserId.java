package com.siemens.cto.aem.common;

import com.siemens.cto.aem.common.domain.model.user.User;

/**
 * Holds {@link User} in a ThreadLocal.
 *
 * Created by JC043760 on 12/29/2015.
 */
public class UserId {

    public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void set(final String id) {
        threadLocal.set(id);
    }

    public static void unset() {
        threadLocal.remove();
    }

    public static String get() {
        return threadLocal.get();
    }

}
