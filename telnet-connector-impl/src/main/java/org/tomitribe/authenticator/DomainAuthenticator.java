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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class DomainAuthenticator implements PasswordAuthenticator {

    private final String domain;

    public DomainAuthenticator(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {

        boolean validUser = false;
        try {
            SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            Object user = securityService.login(domain, username, password);
            if (user != null) {
                validUser = true;
                securityService.logout(user);
            }
        } catch (Exception e) {
            validUser = false;
        }

        return validUser;

    }
}
