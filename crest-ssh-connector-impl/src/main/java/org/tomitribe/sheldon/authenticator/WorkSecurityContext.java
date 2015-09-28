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
package org.tomitribe.sheldon.authenticator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.work.SecurityContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.PasswordValidationCallback;

public class WorkSecurityContext extends SecurityContext {

    private final String username;
    private final String password;
    private boolean authenticated = false;

    public WorkSecurityContext(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void setupSecurityContext(final CallbackHandler handler, final Subject executionSubject, final Subject serviceSubject) {
        List<Callback> callbacks = new ArrayList<Callback>();

        final PasswordValidationCallback pvc = new PasswordValidationCallback(executionSubject, username, password.toCharArray());
        callbacks.add(pvc);

        Callback callbackArray[] = new Callback[callbacks.size()];
        try {
            handler.handle(callbacks.toArray(callbackArray));

        } catch (UnsupportedCallbackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.authenticated = pvc.getResult();
        System.out.println("Authenticated: " + this.authenticated);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}