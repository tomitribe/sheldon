/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.telnet.impl;


import org.tomitribe.crest.Cmd;
import org.tomitribe.crest.Commands;
import org.tomitribe.crest.api.Command;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class TelnetServer implements TtyCodes {

    private final String prompt;

    private final int port;

    private final Map<String, Cmd> commands = new ConcurrentHashMap<String, Cmd>();

    private final AtomicBoolean running = new AtomicBoolean();
    private ServerSocket serverSocket;

    public TelnetServer(String prompt, int port) {
        this.port = port;
        this.prompt = prompt;

        final BuildIn buildIn = new BuildIn();
        for (Method method : Commands.commands(BuildIn.class)) {
            add(new Cmd(buildIn, method));
        }
    }

    public void add(Cmd cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            serverSocket = new ServerSocket(port);
            final Logger logger = Logger.getLogger(TelnetServer.class.getName());
            logger.info("Listening on " + serverSocket.getLocalPort());

            final Socket accept = serverSocket.accept();
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    while (running.get()) {
                        try {
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
            }
        }
    }

    public void session(Socket socket) throws IOException {
        InputStream telnetIn = null;
        PrintStream telnetOut = null;

        try {
            final InputStream in = socket.getInputStream();
            final OutputStream out = socket.getOutputStream();

            telnetIn = new TelnetInputStream(in, out);
            telnetOut = new TelnetPrintStream(out);

            telnetOut.println("");
            telnetOut.println("type \'help\' for a list of commands");


            final DataInputStream dataInputStream = new DataInputStream(telnetIn);

            while (running.get()) {

                prompt(dataInputStream, telnetOut);

            }

        } catch (StopException s) {
            // exit normally
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            close(telnetIn);
            close(telnetOut);
            if (socket != null) socket.close();
        }
    }

    private static void close(Closeable closeable) {
        if (closeable == null) return;

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void prompt(DataInputStream in, PrintStream out) throws StopException {

        try {

            out.print(TTY_Reset + TTY_Bright + this.prompt + " " + TTY_Reset);

            out.flush();

            final String commandline = in.readLine().trim();

            if (commandline.length() < 1) return;

            final List<String> list = new ArrayList<String>();
            Collections.addAll(list, commandline.split(" +"));

            final String command = (list.size() == 0) ? "help" : list.remove(0);
            final String[] args = list.toArray(new String[list.size()]);

            final Cmd cmd = commands.get(command);

            if (cmd == null) {

                out.println("Unknown command: " + command);
                out.println();
                commands.get("help").exec();
                throw new IllegalArgumentException();

            } else {

                try {
                    cmd.exec(args);
                } catch (StopException stop) {
                    throw stop;
                } catch (Throwable throwable) {
                    throwable.printStackTrace(out);
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

    public static class StopException extends RuntimeException {
        public StopException() {
        }

        public StopException(Throwable cause) {
            super(cause);
        }
    }

    public class BuildIn {

        @Command
        public void exit() throws StopException {
            throw new StopException();
        }

        @Command
        public String help() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Commands:\n\n");

            final TreeSet<String> cmds = new TreeSet<String>(commands.keySet());
            for (String command : cmds) {
                sb.append(String.format("   %-20s\n", command));
            }

            return sb.toString();
        }
    }
}
