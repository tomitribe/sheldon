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
package org.tomitribe.sheldon.corejava;

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.util.Files;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SystemPropertiesBeanTest {
    private Main main;

    @Before
    public void setup() {
        this.main = new Main(SystemPropertiesBean.class);
    }

    @Test
    public void testHome() throws Exception {
        assertEquals(System.getProperty("user.home"), main.exec("home"));

        final File newHome = new File(main.exec("home") + File.separator + "kurtcobain");
        Files.mkdir(newHome);

        main.exec("home", newHome.getAbsolutePath());
        assertEquals(newHome.getAbsolutePath(), main.exec("home"));
        newHome.delete();
    }

    @Test
    public void testDir() throws Exception {
        assertEquals(System.getProperty("user.dir"), main.exec("dir"));

        final File newHome = new File(main.exec("dir") + File.separator + "kurtcobain");
        Files.mkdir(newHome);

        main.exec("dir", newHome.getAbsolutePath());
        assertEquals(newHome.getAbsolutePath(), main.exec("dir"));
        newHome.delete();
    }

    @Test
    public void testSetAndGetProperty() throws Exception {
        final String value = (String) main.exec("setProperty", "--key=best.band", "--value=nirvana");
        final String bestBand = (String) main.exec("getProperty", "--key=best.band");
        assertEquals(value, bestBand);
    }

    @Test
    public void testUser() throws Exception {
        assertEquals(System.getProperty("user.name"), main.exec("user"));
    }

    @Test
    public void testJavaVendor() throws Exception {
        assertEquals(System.getProperty("java.vendor"), main.exec("javaVendor"));
    }

    @Test
    public void testJavaVendorUrl() throws Exception {
        assertEquals(System.getProperty("java.vendor.url"), main.exec("javaVendorUrl"));
    }

    @Test
    public void testJavaVersion() throws Exception {
        assertEquals(System.getProperty("java.version"), main.exec("javaVersion"));
    }

    @Test
    public void testOsArch() throws  Exception {
        assertEquals(System.getProperty("os.arch"), main.exec("osArch"));
    }

    @Test
    public void testOsName() throws Exception {
        assertEquals(System.getProperty("os.name"), main.exec("osName"));
    }

    @Test
    public void testOsVersion() throws Exception {
        assertEquals(System.getProperty("os.version"), main.exec("osVersion"));
    }
}
