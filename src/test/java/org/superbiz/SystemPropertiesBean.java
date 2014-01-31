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
package org.superbiz;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.val.Directory;
import org.tomitribe.crest.val.Exists;
import org.tomitribe.telnet.api.TelnetListener;
import org.tomitribe.util.PrintString;

import javax.ejb.MessageDriven;
import java.io.File;
import java.util.Map;
import java.util.TreeSet;

@MessageDriven
public class SystemPropertiesBean implements TelnetListener {

    @Command
    public String home() {
        return System.getProperty("user.home");
    }

    @Command
    public String home(@Exists @Directory File file) {
        return System.setProperty("user.home", file.getAbsolutePath());
    }


    @Command
    public String dir() {
        return System.getProperty("user.dir");
    }

    @Command
    public String dir(@Exists @Directory File file) {
        return System.setProperty("user.dir", file.getAbsolutePath());
    }

}
