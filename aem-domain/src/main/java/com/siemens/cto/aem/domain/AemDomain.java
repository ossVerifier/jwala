package com.siemens.cto.aem.domain;

import com.siemens.cto.aem.domain.model.jvm.JvmState;

public class AemDomain {
    public static final JvmState NO_JVM_IN_PROGRESS_STATE   = null;
    public static final JvmState NO_JVM_COMPLETE_STATE      = null;
    public static final JvmState NO_JVM_FAILURE_STATE       = null;
    public static final String[] NO_JVM_SUCCESS_KEYWORDS    = new String[0];
    public static final JvmState[] EMPTY_JVM_STATES         = new JvmState[0];
}
