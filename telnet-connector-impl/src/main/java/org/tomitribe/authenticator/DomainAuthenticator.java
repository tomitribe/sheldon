/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.authenticator;

import javax.resource.spi.work.WorkException;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.tomitribe.ssh.impl.SshdServer;
import org.tomitribe.ssh.impl.SshdServer.Credential;
import org.tomitribe.telnet.adapter.ContextRunnable;
import org.tomitribe.telnet.adapter.NotAuthenticatedException;

public class DomainAuthenticator implements PasswordAuthenticator {

    private final String domain;
    private final ContextRunnable contextRunnable;

    public DomainAuthenticator(final String domain, final ContextRunnable contextRunnable) {
        this.domain = domain;
        this.contextRunnable = contextRunnable;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        try {
            contextRunnable.run(new Runnable() {

                @Override
                public void run() {
                    // don't actually need to do anything here, just make sure this work object can be run
                    // with the credentials provided
                }
                
            }, username, password, domain);
        } catch (WorkException t) {
            t.printStackTrace();
            return false;
        }
        
        if (session != null) {
            session.setAttribute(SshdServer.CREDENTIAL, new Credential(password));
        }
        
        return true;
    }
}
