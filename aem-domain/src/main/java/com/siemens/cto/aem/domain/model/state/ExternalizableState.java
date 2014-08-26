package com.siemens.cto.aem.domain.model.state;

public interface ExternalizableState {

    String toStateString();

    boolean isTransientState();

}
