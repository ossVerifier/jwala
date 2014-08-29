package com.siemens.cto.aem.domain.model.state;

public interface ExternalizableState {

    String toStateString();

    Transience getTransience();

    Stability getStability();
}
