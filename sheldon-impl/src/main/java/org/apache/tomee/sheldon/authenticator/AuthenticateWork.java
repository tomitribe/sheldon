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
package org.apache.tomee.sheldon.authenticator;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import java.util.Collections;
import java.util.List;

public class AuthenticateWork implements Work, WorkContextProvider {

    private final WorkSecurityContext securityContext;
    private boolean authenticated = false;

    public AuthenticateWork(final String username, final String password) {
        super();
        this.securityContext = new WorkSecurityContext(username, password);
    }

    @Override
    public void run() {
        this.authenticated = this.securityContext.isAuthenticated();
    }

    @Override
    public void release() {
    }

    @Override
    public List<WorkContext> getWorkContexts() {
        return Collections.singletonList((WorkContext) securityContext);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
