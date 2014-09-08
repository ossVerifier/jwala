package com.siemens.cto.aem.domain.model.state;

public interface OperationalState {

    String toStateString();

    Transience getTransience();

    Stability getStability();
}
