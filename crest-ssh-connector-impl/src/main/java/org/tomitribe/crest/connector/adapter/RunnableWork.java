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
package org.tomitribe.crest.connector.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;

import org.tomitribe.crest.connector.authenticator.WorkSecurityContext;

public class RunnableWork implements Work, WorkContextProvider {

    private final List<WorkContext> workContexts = new ArrayList<WorkContext>();
    private final Runnable runnable;
    private final ClassLoader classLoader;

    public RunnableWork(Runnable runnable) {
        this.runnable = runnable;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void release() {

    }

    @Override
    public void run() {
        if (requiresAuthentication() && (! isAuthenticated())) {
            throw new NotAuthenticatedException();
        }
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        runnable.run();
        Thread.currentThread().setContextClassLoader(cl);
    }

    private boolean isAuthenticated() {
        final WorkSecurityContext securityContext = getSecurityContext();
        if (securityContext == null) {
            return false;
        }
        
        return securityContext.isAuthenticated();
    }
    
    private boolean requiresAuthentication() {
        return (getSecurityContext() != null);
    }
    
    private WorkSecurityContext getSecurityContext() {
        final Iterator<WorkContext> iterator = getWorkContexts().iterator();
        while (iterator.hasNext()) {
            WorkContext workContext = (WorkContext) iterator.next();
            if (workContext instanceof WorkSecurityContext) {
                return (WorkSecurityContext) workContext;
            }
        }
        
        return null;
    }

    @Override
    public List<WorkContext> getWorkContexts() {
        return workContexts;
    }
}