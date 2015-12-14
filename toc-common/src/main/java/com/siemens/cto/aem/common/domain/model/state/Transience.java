package com.siemens.cto.aem.common.domain.model.state;

public enum Transience {

    TRANSIENT(true),
    PERMANENT(false);

    private final boolean isTransient;

    private Transience(final boolean shouldBeTransient) {
        isTransient = shouldBeTransient;
    }

    public boolean isTransient() {
        return isTransient;
    }
}
