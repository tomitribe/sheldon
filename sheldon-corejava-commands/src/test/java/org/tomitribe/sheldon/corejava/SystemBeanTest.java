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

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SystemBeanTest {

    private Main main;

    @Before
    public void setup() {
        this.main = new Main(SystemBean.class);
    }

    @Test
    public void testDateCommand() throws Exception {
        final String date = (String) main.exec("date");
        assertNotNull(date);
        assertTrue(date.contains(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ""));
        assertTrue(date.contains(Calendar.getInstance().get(Calendar.YEAR) + ""));
    }

    @Test
    public void testEnv() throws Exception {
        final String env = (String) main.exec("env");
        assertNotNull(env);
        final Set<String> systemEnvKeys = System.getenv().keySet();
        for (String key : systemEnvKeys) {
            assertTrue(env.contains(key));
        }
    }

    @Test
    public void testProperties() throws Exception {
        final String properties = (String) main.exec("properties");
        assertNotNull(properties);
        final Enumeration<?> property = System.getProperties().propertyNames();
        while(property.hasMoreElements()) {
            assertTrue(properties.contains(property.nextElement() + ""));
        }
    }
}
