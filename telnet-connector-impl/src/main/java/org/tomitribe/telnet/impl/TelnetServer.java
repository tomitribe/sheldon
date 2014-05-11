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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class TelnetServer implements TtyCodes {

    private final int port;
    private final ConsoleSession session;
    private final AtomicBoolean running = new AtomicBoolean();
    private ServerSocket serverSocket;

    public TelnetServer(ConsoleSession session, int port) {
        this.session = session;
        this.port = port;
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

    public void session(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        try {
            session.doSession(in, out, false);
        } catch (StopException s) {
            // exit normally
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            close(in);
            close(out);
            if (socket != null)
                socket.close();
        }
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
