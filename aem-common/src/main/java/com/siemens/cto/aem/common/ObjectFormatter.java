package com.siemens.cto.aem.common;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by z002xuvs on 11/29/13.
 */
public class ObjectFormatter {

    public static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss a";

    protected Object object;
    protected boolean doPrintAll = true;

    public ObjectFormatter(final Object object) {
        this.object = object;
    }

    public String format() {
        if (object == null) {
            return null;
        }

        final StringBuilder builder = new StringBuilder(className());
        builder.append('[');

        formatFields(builder);

        builder.append(']');
        return builder.toString();
    }

    protected void formatFields(final StringBuilder builder) {
        String separator = "";
        final Field[] fields = object.getClass().getDeclaredFields();
        for (final Field field : fields) {
            addField(builder, field, separator);
            separator = ", ";
        }
    }

    public boolean printAll() {
        return doPrintAll;
    }

    public void setDoPrintAll(final boolean doPrintAll) {
        this.doPrintAll = doPrintAll;
    }

    protected boolean ignoreField(final Field f) {
        return f.getName().startsWith("$");
    }

    protected void addField(final StringBuilder builder, final Field f, final String separator) {
        if (ignoreField(f)) {
            return;
        }

        builder.append(separator);
        builder.append(f.getName());
        builder.append("=");
        builder.append(getValue(f));
    }

    protected Object getValue(final Field f) {
        if (f.getType().equals(Calendar.class)) {
            return formatDate(f);
        } else {
            return fieldValue(f);
        }
    }

    protected String formatDate(final Field f) {
        return new SimpleDateFormat(DATE_FORMAT).format(((Calendar) fieldValue(f)).getTime());
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected Object fieldValue(final Field f) {
        if (printAll()) {
            f.setAccessible(true);
        }

        try {
            return f.get(object);
        } catch (final IllegalAccessException e) {
            // do nothing
        }

        return null;
    }

    protected String className() {
        final Class clazz = object.getClass();
        return clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1, clazz.getName().length());
    }
}
