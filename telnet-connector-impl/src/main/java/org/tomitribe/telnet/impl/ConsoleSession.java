/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.telnet.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.tomitribe.telnet.util.Utils;
import jline.Terminal;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.tomitribe.crest.CommandFailedException;
import org.tomitribe.crest.Environment;
import org.tomitribe.crest.Main;

public class ConsoleSession implements TtyCodes {

    private final Main main;
    private final String prompt;

    public ConsoleSession(Main main, String prompt) {
        super();
        this.main = main;
        this.prompt = prompt;
    }

    public void doSession(InputStream in, OutputStream out, boolean ssh) throws IOException {
        FilterOutputStream fo = new FilterOutputStream(out) {
            @Override
            public void write(final int i) throws IOException {
                super.write(i);

                // workaround for MacOSX!! reset line after CR..
                if (Utils.isMac() && i == ConsoleReader.CR.toCharArray()[0]) {
                    super.write(ConsoleReader.RESET_LINE);
                }
            }
        };

        Terminal term = ssh ? null : new UnsupportedTerminal();
        ConsoleReader reader = new ConsoleReader(in, fo, term);

        reader.setPrompt(prompt);
        PrintWriter writer = new PrintWriter(reader.getOutput());
        writer.println("");
        writer.println("type \'help\' for a list of commands");

        String line;
        try {
            while ((line = reader.readLine().trim()) != null) {
                if (line.length() > 0) {
                    handleUserInput(line.trim(), in, fo);
                }
            }
        } catch (StopException stop) {
            throw stop;
        } catch (UnsupportedOperationException e) {
            throw new StopException(e);
        } catch (Throwable e) {
            e.printStackTrace(new PrintStream(out));
            throw new StopException(e);
        }
    }

    private void handleUserInput(String commandline, InputStream in, OutputStream out) {
        final String[] args = commandline.split(" +");
        PrintStream ps = new PrintStream(out);

        try {
            final Environment env = new ConsoleEnvironment(ps, in);
            main.main(env, args);
        } catch (CommandFailedException e) {
            if (e.getCause() instanceof StopException) {
                throw (StopException) e.getCause();
            }

            ps.println("Command Bean threw an Exception");
            e.printStackTrace(ps);
        } catch (IllegalArgumentException iae) {
            // no-op
        } catch (StopException stop) {
            throw stop;
        } catch (Throwable throwable) {
            throwable.printStackTrace(ps);
        }
    }

}
