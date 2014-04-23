/*
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
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.tomitribe.crest.Cmd;
import org.tomitribe.crest.Commands;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.Target;
import org.tomitribe.ssh.impl.SshdServer;
import org.tomitribe.telnet.impl.BuildIn;
import org.tomitribe.telnet.impl.ConsoleSession;
import org.tomitribe.telnet.impl.TelnetServer;

@Connector(description = "Telnet ResourceAdapter", 
           displayName = "Telnet ResourceAdapter", 
           eisType = "Telnet Adapter", 
           version = "1.0")
public class TelnetResourceAdapter implements javax.resource.spi.ResourceAdapter {

    private TelnetServer telnetServer;
    private SshdServer sshdServer;

    /**
     * Corresponds to the ra.xml <config-property>
     */
    @ConfigProperty(defaultValue = "prompt>")
    @NotNull
    private String prompt;
    
    @Size(min = 1, max = 0xFFFF)
    @ConfigProperty
    private Integer sshPort;

    @Size(min = 1, max = 0xFFFF)
    @ConfigProperty
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

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        
        main = new Main();
        
        // add built-in commands
        final Map<String, Cmd> commands = Commands.get(new BuildIn());
        for (Cmd cmd : commands.values()) {
            main.add(cmd);
        }
        
        session = new ConsoleSession(main, prompt);
        
        if (sshPort != null) {
          sshdServer = new SshdServer(session, sshPort);
          sshdServer.start();
        }
        
        if (telnetPort != null) {
            telnetServer = new TelnetServer(session, telnetPort);
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

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
            throws ResourceException {
        final TelnetActivationSpec telnetActivationSpec = (TelnetActivationSpec) activationSpec;

        final MessageEndpoint messageEndpoint = messageEndpointFactory.createEndpoint(null);

        final EndpointTarget target = new EndpointTarget(messageEndpoint);
        target.commands.addAll(Commands.get(telnetActivationSpec.getBeanClass(), target, null).values());

        for (Cmd cmd : target.commands) {
            main.add(cmd);
        }

        targets.put(telnetActivationSpec, target);
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

    final Map<TelnetActivationSpec, EndpointTarget> targets = 
            new ConcurrentHashMap<TelnetActivationSpec, EndpointTarget>();



    private static class EndpointTarget implements Target {
        private final MessageEndpoint messageEndpoint;
        private final List<Cmd> commands = new ArrayList<Cmd>();

        public EndpointTarget(MessageEndpoint messageEndpoint) {
            this.messageEndpoint = messageEndpoint;
        }

        @Override
        public Object invoke(Method method, Object... objects) 
                throws InvocationTargetException, IllegalAccessException {

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
}
