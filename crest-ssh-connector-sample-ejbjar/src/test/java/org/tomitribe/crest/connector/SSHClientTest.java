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
package org.tomitribe.crest.connector;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector10.ConfigProperty;
import org.jboss.shrinkwrap.descriptor.api.connector10.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector10.Resourceadapter;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@RunWith(Arquillian.class)
public class SSHClientTest {

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final File[] deps = Maven.resolver().resolve("org.tomitribe:tomitribe-util:1.1.0",
                "org.tomitribe:tomitribe-crest-api:0.3").withTransitivity().asFile();

        final File[] rarDeps = Maven.resolver().resolve("org.apache.sshd:sshd-core:0.9.0",
                "jline:jline:2.11",
                "org.tomitribe:tomitribe-crest:0.3",
                "org.tomitribe:tomitribe-util:1.1.0").withTransitivity().asFile();
        
        final JavaArchive apiJar = ShrinkWrap.create(JavaArchive.class, "api.jar");
        apiJar.addPackages(true, "org.tomitribe.crest.connector.api");
        System.out.println(apiJar.toString(true));
        System.out.println();
        
        final JavaArchive rarLib = ShrinkWrap.create(JavaArchive.class, "lib.jar");
        rarLib.addPackages(false,
                "org.tomitribe.crest.connector.cdi",
                "org.tomitribe.crest.connector.ssh",
                "org.tomitribe.crest.connector.adapter",
                "org.tomitribe.crest.connector.telnet",
                "org.tomitribe.crest.connector.util",
                "org.tomitribe.crest.connector.commands.factories",
                "org.tomitribe.crest.connector.authenticator",
                "org.tomitribe.crest.connector.commands");
        
        System.out.println(rarLib.toString(true));
        System.out.println();

        final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar");
        rar.addAsLibraries(rarLib).addAsLibraries(rarDeps);

        final ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class);
        final ConfigProperty<Resourceadapter<ConnectorDescriptor>> sshPortProperty = raXml.getOrCreateResourceadapter().createConfigProperty();
        sshPortProperty.configPropertyName("sshPort");
        sshPortProperty.configPropertyType("int");
        sshPortProperty.configPropertyValue("2222");

        final ConfigProperty<Resourceadapter<ConnectorDescriptor>> telnetPortProperty = raXml.getOrCreateResourceadapter().createConfigProperty();
        telnetPortProperty.configPropertyName("telnetPort");
        telnetPortProperty.configPropertyType("int");
        telnetPortProperty.configPropertyValue("2020");

        rar.setResourceAdapterXML(new StringAsset(raXml.exportAsString()));
        System.out.println(rar.toString(true));
        System.out.println();

        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar");
        jar.addPackages(true, "org.superbiz");
        System.out.println(jar.toString(true));
        System.out.println();

        // Make the EAR
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsModule(rar).addAsModule(jar).addAsLibraries(apiJar).addAsLibraries(deps);
        System.out.println(ear.toString(true));
        System.out.println();

        return ear;
    }
    
    @Test
    public void testShouldConnectViaSSHAndRunCommand() throws Exception {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        ClientSession session = client.connect("localhost", 2222).await().getSession();
        session.authPassword("tomee", "tomee").await().isSuccess();
        ClientChannel channel = session.createChannel(ClientChannel.CHANNEL_SHELL);

        PipedOutputStream pipedIn = new PipedOutputStream();
        channel.setIn(new PipedInputStream(pipedIn));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        channel.setOut(out);
        channel.setErr(err);
        channel.open();

        pipedIn.write("whoami\n".getBytes());
        pipedIn.flush();

        pipedIn.write("exit\n".getBytes());
        pipedIn.flush();

        channel.waitFor(ClientChannel.CLOSED, 0);

        channel.close(false);
        client.stop();

        final String responseReceived = out.toString();
        assertContains("tomee", responseReceived);
    }

    private void assertContains(String lineToFind, String responseReceived) {
        boolean matchFound = false;
        
        final String[] lines = responseReceived.split("\\n");
        for (String line : lines) {
            if (line.trim().equals(lineToFind)) {
                matchFound = true;
            }
        }
        
        if (! matchFound) {
            Assert.fail("Line: " + lineToFind + " not found");
        }
    }
}
