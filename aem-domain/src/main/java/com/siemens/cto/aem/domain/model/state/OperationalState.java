package com.siemens.cto.aem.domain.model.state;

public interface OperationalState {

    String toStateString();

    String toPersistentString();

    Transience getTransience();

    Stability getStability();
}
