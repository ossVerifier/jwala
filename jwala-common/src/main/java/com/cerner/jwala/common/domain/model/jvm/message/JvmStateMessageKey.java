package com.cerner.jwala.common.domain.model.jvm.message;

public enum JvmStateMessageKey {

    JVM_ID("jvm_id");

    private final String key;

    private JvmStateMessageKey(final String theKey) {
        key = theKey;
    }

    public String getKey() {
        return key;
    }
}
