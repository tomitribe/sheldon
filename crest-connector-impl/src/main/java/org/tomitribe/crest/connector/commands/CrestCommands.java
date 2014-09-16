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
package org.tomitribe.crest.connector.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.tomitribe.crest.connector.adapter.SecurityHandler;
import org.tomitribe.crest.connector.ssh.SshdServer;
import org.tomitribe.crest.connector.telnet.ConsoleSession;
import org.tomitribe.crest.connector.telnet.StopException;
import org.tomitribe.crest.connector.telnet.TtyCodes;

public class CrestCommands implements Command, Runnable, TtyCodes, SessionAware {

    private OutputStream err;
    private ExitCallback cbk;
    private InputStream in;
    private OutputStream out;
    private Environment env;
    private Thread thread;

    private final ConsoleSession consoleSession;
    private final SecurityHandler contextRunnable;
    private ServerSession session;

    public CrestCommands(ConsoleSession session, SecurityHandler contextRunnable) {
        super();
        this.consoleSession = session;
        this.contextRunnable = contextRunnable;
    }

    @Override
    public void destroy() {
        thread.interrupt();
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback cbk) {
        this.cbk = cbk;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void start(Environment env) throws IOException {
        this.env = env;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        contextRunnable.runWithSecurityContext(new Runnable() {

            @Override
            public void run() {
                try {
                    consoleSession.doSession(in, out, true);
                } catch (StopException s) {
                    // exit normally
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                cbk.onExit(0);
            }
        }, session.getUsername(), session.getAttribute(SshdServer.CREDENTIAL).getValue());
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }
}
