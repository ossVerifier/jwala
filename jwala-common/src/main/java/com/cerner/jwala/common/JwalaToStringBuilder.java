package com.cerner.jwala.common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by Steven Ger on 12/20/16.
 */
public class JwalaToStringBuilder extends ToStringBuilder {

    private ToStringBuilder toStringBuilder;

    public JwalaToStringBuilder(Object object) {
        super(object, ToStringStyle.SHORT_PREFIX_STYLE);
        this.toStringBuilder = new ToStringBuilder(object);
    }

    @Override
    public ToStringBuilder append(boolean value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(boolean[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(byte value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(byte[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(char value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(char[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(double value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(double[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(float value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(float[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(int value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(int[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(long value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(long[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(Object obj) {
        return toStringBuilder.append(obj);
    }

    @Override
    public ToStringBuilder append(Object[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(short value) {
        return toStringBuilder.append(value);
    }

    @Override
    public ToStringBuilder append(short[] array) {
        return toStringBuilder.append(array);
    }

    @Override
    public ToStringBuilder append(String fieldName, boolean value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, boolean[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, boolean[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, byte value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, byte[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, byte[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, char value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, char[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, char[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, double value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, double[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, double[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, float value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, float[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, float[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, int value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, int[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, int[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, long value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, long[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, long[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, Object obj) {
        return toStringBuilder.append(fieldName, obj);
    }

    @Override
    public ToStringBuilder append(String fieldName, Object obj, boolean fullDetail) {
        return toStringBuilder.append(fieldName, obj, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, Object[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, Object[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder append(String fieldName, short value) {
        return toStringBuilder.append(fieldName, value);
    }

    @Override
    public ToStringBuilder append(String fieldName, short[] array) {
        return toStringBuilder.append(fieldName, array);
    }

    @Override
    public ToStringBuilder append(String fieldName, short[] array, boolean fullDetail) {
        return toStringBuilder.append(fieldName, array, fullDetail);
    }

    @Override
    public ToStringBuilder appendAsObjectToString(Object srcObject) {
        return toStringBuilder.appendAsObjectToString(srcObject);
    }

    @Override
    public ToStringBuilder appendSuper(String superToString) {
        return toStringBuilder.appendSuper(superToString);
    }

    @Override
    public ToStringBuilder appendToString(String toString) {
        return toStringBuilder.appendToString(toString);
    }

    @Override
    public Object getObject() {
        return toStringBuilder.getObject();
    }

    @Override
    public StringBuffer getStringBuffer() {
        return toStringBuilder.getStringBuffer();
    }

    @Override
    public ToStringStyle getStyle() {
        return toStringBuilder.getStyle();
    }

    @Override
    public String toString() {
        return toStringBuilder.toString();
    }

    @Override
    public String build() {
        return toStringBuilder.build();
    }
}
