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
package org.tomitribe.sheldon.ssh;

import org.tomitribe.crest.environments.Environment;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

class ConsoleEnvironment implements Environment {
    private final Map<Class<?>, Object> services;
    private final PrintStream out;
    private final InputStream in;

    public ConsoleEnvironment(PrintStream out, InputStream in) {
        this.out = out;
        this.in = in;
        this.services = Collections.emptyMap();
    }

    @Override
    public PrintStream getOutput() {
        return out;
    }

    @Override
    public PrintStream getError() {
        return out;
    }

    @Override
    public InputStream getInput() {
        return in;
    }

    @Override
    public Properties getProperties() {
        return System.getProperties();
    }
}
