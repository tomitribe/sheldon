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

import static org.junit.Assert.assertNotNull;

public class RuntimeBeanTest {
    private Main main;

    @Before
    public void setup() {
        this.main = new Main(RuntimeBean.class);
    }

    @Test
    public void testFreeMemory() throws Exception {
        final String freeMemory = (String) main.exec("freeMemory");
        assertNotNull(freeMemory);
    }

    @Test
    public void testMaxMemory() throws Exception {
        final String maxMemory = (String) main.exec("maxMemory");
        assertNotNull(maxMemory);
    }

    @Test
    public void testTotalMemory() throws Exception {
        final String totalMemory = (String) main.exec("totalMemory");
        assertNotNull(totalMemory);
    }

    @Test
    public void testThreads() throws Exception {
        final String threads = (String) main.exec("threads");
        assertNotNull(threads);
    }

}
