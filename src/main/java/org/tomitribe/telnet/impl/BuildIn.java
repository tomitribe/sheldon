package org.tomitribe.telnet.impl;

import org.tomitribe.crest.api.Command;

public class BuildIn {
    @Command
    public void exit() throws StopException {
        throw new StopException();
    }
}