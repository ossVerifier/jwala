package com.siemens.cto.aem.domain.model.exec;

public class ShellCommand extends ExecCommand {

    public ShellCommand(final String... theCommandFragments) {
        super(theCommandFragments);
        this.runInShell = true;
    }

}
