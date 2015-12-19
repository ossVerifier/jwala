package com.siemens.cto.aem.control.command;

public interface PlatformCommandProvider<T> {

    ServiceCommandBuilder getServiceCommandBuilderFor(final T anOperation);
}
