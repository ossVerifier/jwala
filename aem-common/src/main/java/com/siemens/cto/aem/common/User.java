package com.siemens.cto.aem.common;

public class User {

    private static ThreadLocal<User> threadLocalUser = new ThreadLocal<User>();

    public String userName;
    public String password;

    public User(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }

    public void addToThread() {
        threadLocalUser.set(this);
    }

    public static User getUser() {
        return threadLocalUser.get();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public static void invalidate() {
        threadLocalUser.set(null);
    }

    public static boolean hasUser() {
        return threadLocalUser != null && threadLocalUser.get() != null;
    }

}
