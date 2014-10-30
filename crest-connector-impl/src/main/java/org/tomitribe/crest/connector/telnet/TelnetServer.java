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
package org.tomitribe.crest.connector.telnet;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.tomitribe.crest.connector.adapter.SecurityHandler;
import org.tomitribe.crest.connector.authenticator.PasswordAuthenticatorImpl;
import org.tomitribe.crest.connector.util.Utils;

public class TelnetServer implements TtyCodes {

    private final int port;
    private final SecurityHandler contextRunner;
    private final ConsoleSession session;
    private final AtomicBoolean running = new AtomicBoolean();
    private ServerSocket serverSocket;

    public TelnetServer(ConsoleSession session, int port, SecurityHandler securityHandler) {
        this.session = session;
        this.port = port;
        this.contextRunner = securityHandler;
    }

    public void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            serverSocket = new ServerSocket(port);
            final Logger logger = Logger.getLogger(TelnetServer.class.getName());
            logger.info("Listening on " + serverSocket.getLocalPort());

            final Thread thread = new Thread() {
                @Override
                public void run() {
                    while (running.get()) {
                        try {
                            final Socket accept = serverSocket.accept();
                            session(accept);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
    }

    public void stop() throws IOException {
        if (running.compareAndSet(true, false)) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }

    public void session(final Socket socket) throws IOException {
        final InputStream in = socket.getInputStream();
        final OutputStream out = socket.getOutputStream();

        FilterOutputStream fo = new FilterOutputStream(out) {
            @Override
            public void write(final int i) throws IOException {
                super.write(i);

                // workaround for MacOSX!! reset line after CR..
                if (!Utils.isWin() && i == ConsoleReader.CR.toCharArray()[0]) {
                    super.write(ConsoleReader.RESET_LINE);
                }
            }
        };

        final ConsoleReader consoleReader = new ConsoleReader(in, fo, new UnsupportedTerminal());
        final String username = consoleReader.readLine("login:");
        final String password = consoleReader.readLine("password:", new Character((char) 0));
        try {
            if (!new PasswordAuthenticatorImpl(contextRunner).authenticate(username, password, null)) {
                consoleReader.println("login failed ");
                consoleReader.flush();

                // close the connection
                close(in);
                close(out);
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return;
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        contextRunner.runWithSecurityContext(new Runnable() {

            @Override
            public void run() {
                try {
                    session.doSession(in, out, false);
                } catch (StopException s) {
                    // exit normally
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    close(in);
                    close(out);
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }, username, password);
    }

    private static void close(Closeable closeable) {
        if (closeable == null)
            return;

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
