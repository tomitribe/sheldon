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
package org.tomitribe.telnet.adapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import javax.validation.constraints.NotNull;

import org.tomitribe.authenticator.AuthenticateWork;
import org.tomitribe.authenticator.WorkSecurityContext;
import org.tomitribe.crest.Cmd;
import org.tomitribe.crest.Commands;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.Target;
import org.tomitribe.ssh.impl.SshdServer;
import org.tomitribe.telnet.impl.BuildIn;
import org.tomitribe.telnet.impl.ConsoleSession;
import org.tomitribe.telnet.impl.TelnetServer;

@Connector(description = "Telnet ResourceAdapter", displayName = "Telnet ResourceAdapter", eisType = "Telnet Adapter", version = "1.0")
public class TelnetResourceAdapter implements ResourceAdapter, SecurityHandler {

    private TelnetServer telnetServer;
    private SshdServer sshdServer;

    /**
     * Corresponds to the ra.xml <config-property>
     */
    @ConfigProperty(defaultValue = "prompt>")
    @NotNull
    private String prompt;

    @ConfigProperty(defaultValue = "UserDatabase")
    @NotNull
    private String domain;

    @ConfigProperty(defaultValue = "2222")
    private Integer sshPort;

    @ConfigProperty(defaultValue = "2020")
    private Integer telnetPort;

    private Main main;
    private ConsoleSession session;

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public int getTelnetPort() {
        return telnetPort;
    }

    public void setTelnetPort(int telnetPort) {
        this.telnetPort = telnetPort;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {

        workManager = bootstrapContext.getWorkManager();
        main = new Main();

        // add built-in commands
        final Map<String, Cmd> commands = Commands.get(new BuildIn());
        for (Cmd cmd : commands.values()) {
            main.add(cmd);
        }

        session = new ConsoleSession(main, prompt);

        if (sshPort != null) {
            sshdServer = new SshdServer(session, sshPort, domain, this);
            sshdServer.start();
        }

        if (telnetPort != null) {
            telnetServer = new TelnetServer(session, telnetPort, domain, this);
            try {
                telnetServer.start();
            } catch (IOException e) {
                throw new ResourceAdapterInternalException(e);
            }
        }
    }

    public void stop() {
        try {
            if (telnetServer != null) {
                telnetServer.stop();
            }
        } catch (IOException e) {
            // TODO log this... oh wait, no standard way to do that
            e.printStackTrace();
        }

        if (sshdServer != null) {
            sshdServer.stop();
        }
    }

    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec)
            throws ResourceException
    {
        final TelnetActivationSpec telnetActivationSpec = (TelnetActivationSpec) activationSpec;

        workManager.scheduleWork(new Work() {

            @Override
            public void run() {
                try {
                    final MessageEndpoint messageEndpoint = messageEndpointFactory.createEndpoint(null);

                    final EndpointTarget target = new EndpointTarget(messageEndpoint);
                    final Class<?> endpointClass = telnetActivationSpec.getBeanClass() != null ? telnetActivationSpec
                            .getBeanClass() : messageEndpointFactory.getEndpointClass();

                    target.commands.addAll(Commands.get(endpointClass, target, null).values());

                    for (Cmd cmd : target.commands) {
                        main.add(cmd);
                    }

                    targets.put(telnetActivationSpec, target);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void release() {
            }

        });

    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        final TelnetActivationSpec telnetActivationSpec = (TelnetActivationSpec) activationSpec;

        final EndpointTarget endpointTarget = targets.get(telnetActivationSpec);
        if (endpointTarget == null) {
            throw new IllegalStateException("No EndpointTarget to undeploy for ActivationSpec " + activationSpec);
        }

        final List<Cmd> commands = telnetActivationSpec.getCommands();
        for (Cmd command : commands) {
            main.remove(command);
        }

        endpointTarget.messageEndpoint.release();
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    final Map<TelnetActivationSpec, EndpointTarget> targets = new ConcurrentHashMap<TelnetActivationSpec, EndpointTarget>();
    private WorkManager workManager;

    @Override
    public void runWithSecurityContext(Runnable runnable, String username, String password, String domain) {

        // create a work with a security context
        RunnableWork runnableWork = new RunnableWork(runnable);
        runnableWork.getWorkContexts().add(new WorkSecurityContext(username, password, domain));

        // get the work manager to execute asynchronously
        try {
            workManager.startWork(runnableWork);
        } catch (WorkException e) {
            e.printStackTrace();
        }
    }

    private static class EndpointTarget implements Target {
        private final MessageEndpoint messageEndpoint;
        private final List<Cmd> commands = new ArrayList<Cmd>();

        public EndpointTarget(MessageEndpoint messageEndpoint) {
            this.messageEndpoint = messageEndpoint;
        }

        @Override
        public Object invoke(Method method, Object... objects) 
                throws InvocationTargetException, IllegalAccessException
        {

            try {
                try {
                    messageEndpoint.beforeDelivery(method);

                    return method.invoke(messageEndpoint, objects);
                } finally {
                    messageEndpoint.afterDelivery();
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((prompt == null) ? 0 : prompt.hashCode());
        result = prime * result + ((sshPort == null) ? 0 : sshPort.hashCode());
        result = prime * result + ((telnetPort == null) ? 0 : telnetPort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TelnetResourceAdapter other = (TelnetResourceAdapter) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (prompt == null) {
            if (other.prompt != null)
                return false;
        } else if (!prompt.equals(other.prompt))
            return false;
        if (sshPort == null) {
            if (other.sshPort != null)
                return false;
        } else if (!sshPort.equals(other.sshPort))
            return false;
        if (telnetPort == null) {
            if (other.telnetPort != null)
                return false;
        } else if (!telnetPort.equals(other.telnetPort))
            return false;
        return true;
    }

    @Override
    public boolean authenticate(String username, String password, String domain) {
        boolean authenticated = false;
        
        final AuthenticateWork authenticateWork = new AuthenticateWork(username, password, domain);
        try {
            workManager.doWork(authenticateWork);
            System.out.println("Work finished - result:" + authenticateWork.isAuthenticated());
            authenticated = authenticateWork.isAuthenticated();
        } catch (WorkException e) {
            authenticated = false;
        }

        if (! authenticated) {
            return false;
        }
        
        return authenticated;
    }
    
    
}
