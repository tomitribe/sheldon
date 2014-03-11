package org.tomitribe.telnet.impl;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.tomitribe.crest.Environment;

class ConsoleEnvironment implements Environment {
    private final PrintStream out;
    private final InputStream in;

    public ConsoleEnvironment(PrintStream out, InputStream in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public PrintStream getOutput() {
        return out;
    }

    @Override
    public PrintStream getError() {
        return out;
    }

    @Override
    public InputStream getInput() {
        return in;
    }

    @Override
    public Properties getProperties() {
        return System.getProperties();
    }
}