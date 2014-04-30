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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.telnet;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class Runner {

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        File[] deps = Maven.resolver().resolve("org.tomitribe:tomitribe-crest:1.0.0-SNAPSHOT", "org.tomitribe:tomitribe-util:1.0.0-SNAPSHOT").withTransitivity().asFile();
        File[] rarDeps = Maven.resolver().resolve("org.apache.sshd:sshd-core:0.9.0", "jline:jline:2.11").withTransitivity().asFile();
        
        final JavaArchive apiJar = ShrinkWrap.create(JavaArchive.class, "api.jar");
        apiJar.addPackages(true, "org.tomitribe.telnet.api");
        System.out.println(apiJar.toString(true));
        System.out.println();

        
        final JavaArchive rarLib = ShrinkWrap.create(JavaArchive.class, "lib.jar");
        rarLib.addPackages(false, "org.tomitribe.ssh.impl", "org.tomitribe.telnet.adapter", "org.tomitribe.telnet.impl");
//        rarLib.setManifest(new StringAsset("Class-Path: api.jar"));
        
        System.out.println(rarLib.toString(true));
        System.out.println();

        final ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar");
        rar.addAsLibraries(rarLib).addAsLibraries(rarDeps);

        ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class);
        ConfigProperty<Resourceadapter<ConnectorDescriptor>> sshPortProperty = raXml.getOrCreateResourceadapter().createConfigProperty();
        sshPortProperty.configPropertyName("sshPort");
        sshPortProperty.configPropertyType("int");
        sshPortProperty.configPropertyValue("2222");

        ConfigProperty<Resourceadapter<ConnectorDescriptor>> telnetPortProperty = raXml.getOrCreateResourceadapter().createConfigProperty();
        telnetPortProperty.configPropertyName("telnetPort");
        telnetPortProperty.configPropertyType("int");
        telnetPortProperty.configPropertyValue("2020");

        ConfigProperty<Resourceadapter<ConnectorDescriptor>> domainProperty = raXml.getOrCreateResourceadapter().createConfigProperty();
        domainProperty.configPropertyName("domain");
        domainProperty.configPropertyType("java.lang.String");
        domainProperty.configPropertyValue("UserDatabase");
        
        rar.setResourceAdapterXML(new StringAsset(raXml.exportAsString()));
        System.out.println(rar.toString(true));
        System.out.println();

        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar");
        jar.addPackages(true, "org.superbiz");
//        jar.setManifest(new StringAsset("Class-Path: api.jar"));
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
    public void run(){
        try {
            Thread.sleep(TimeUnit.HOURS.toMillis(1));
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}
