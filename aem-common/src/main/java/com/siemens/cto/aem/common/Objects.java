package com.siemens.cto.aem.common;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: z002xuvs Date: 11/8/13 Time: 7:25 PM To change this template use File | Settings |
 * File Templates.
 */
public class Objects {

    public static String surround(final Object o) {
        return "'" + o + "'";
    }

    public static boolean isEmpty(final String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isEmpty(final List l) {
        return l == null || l.size() == 0;
    }

    public static boolean isEmpty(final Object[] o) {
        return o == null || o.length == 0;
    }

    public static boolean isEmpty(final Collection c) {
        return c == null || c.size() == 0;
    }

    public static boolean notEmpty(final Collection c) {
        return c != null && c.size() > 0;
    }

    public static boolean notEmpty(final String s) {
        return s != null && s.trim().length() > 0;
    }

    public static int size(final Collection c) {
        if (isEmpty(c)) {
            return 0;
        }

        return c.size();
    }

    private Objects() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }
}
