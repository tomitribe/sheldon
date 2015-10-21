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

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.val.Directory;
import org.tomitribe.crest.val.Exists;
import org.tomitribe.sheldon.api.CommandListener;

import javax.ejb.MessageDriven;
import java.io.File;

@MessageDriven(name = "SystemProperties")
public class SystemPropertiesBean implements CommandListener {

    @Command
    public String home() {
        return System.getProperty("user.home");
    }

    @Command
    public String home(@Exists @Directory File file) {
        return System.setProperty("user.home", file.getAbsolutePath());
    }

    @Command
    public String user() {
        return System.getProperty("user.name");
    }

    @Command
    public String dir() {
        return System.getProperty("user.dir");
    }

    @Command
    public String dir(@Exists @Directory File file) {
        return System.setProperty("user.dir", file.getAbsolutePath());
    }

    @Command
    public String setProperty(@Option({"key", "k"}) String key, @Option({"value", "v"}) String value) {
        final String valueReturn = System.setProperty(key, value);
        if (valueReturn == null) {
            return value;
        }
        return valueReturn;
    }

    @Command
    public String getProperty(@Option({"key", "k"}) String key) {
        return System.getProperty(key);
    }

    @Command
    public String javaVendor() {
        return System.getProperty("java.vendor");
    }

    @Command
    public String javaVendorUrl() {
        return System.getProperty("java.vendor.url");
    }

    @Command
    public String javaVersion() {
        return System.getProperty("java.version");
    }

    @Command
    public String osArch() {
        return System.getProperty("os.arch");
    }

    @Command
    public String osName() {
        return System.getProperty("os.name");
    }

    @Command
    public String osVersion() {
        return System.getProperty("os.version");
    }
}
