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
package org.tomitribe.ssh.impl;

import java.io.File;
import java.io.IOException;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.tomitribe.authenticator.DomainAuthenticator;
import org.tomitribe.commands.factories.TomEEComandsFactory;
import org.tomitribe.telnet.adapter.ContextRunnable;
import org.tomitribe.telnet.impl.ConsoleSession;

public class SshdServer {

    public static class Credential {
        private final String value;

        public Credential(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final Session.AttributeKey<Credential> CREDENTIAL = new Session.AttributeKey<Credential>();
    private static final String KEY_NAME = "ssh-key";
    
    private SshServer sshServer;
    private final ConsoleSession session;
    private final int port;
    private final String domain;
    private final ContextRunnable contextRunnable;

    public SshdServer(ConsoleSession session, int port, String domain, ContextRunnable contextRunnable) {
        this.session = session;
        this.port = port;
        this.domain = domain;
        this.contextRunnable = contextRunnable;
    }

    public void start() {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setHost("0.0.0.0");

        final String basePath = new File(System.getProperty("user.dir")).getAbsolutePath();
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshServer.setKeyPairProvider(new PEMGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".pem")
                    .getPath()));
        } else {
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".ser")
                    .getPath()));
        }

        sshServer.setShellFactory(new TomEEComandsFactory(session, domain, contextRunnable));
        sshServer.setPasswordAuthenticator(new DomainAuthenticator(domain, contextRunnable));

        try {
            sshServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            sshServer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
