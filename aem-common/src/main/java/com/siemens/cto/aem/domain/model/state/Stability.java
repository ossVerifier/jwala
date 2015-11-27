package com.siemens.cto.aem.domain.model.state;

public enum Stability {

    STABLE(true),
    UNSTABLE(false);

    private final boolean isStable;

    private Stability(final boolean shouldBeStable) {
        isStable = shouldBeStable;
    }

    public boolean isStable() {
        return isStable;
    }
}
