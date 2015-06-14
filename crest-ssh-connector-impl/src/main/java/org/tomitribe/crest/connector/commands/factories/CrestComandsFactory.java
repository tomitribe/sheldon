/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.crest.connector.commands.factories;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.tomitribe.crest.connector.adapter.SecurityHandler;
import org.tomitribe.crest.connector.commands.CrestCommands;
import org.tomitribe.crest.connector.ssh.ConsoleSession;

public class CrestComandsFactory implements Factory<Command> {

    private final ConsoleSession session;
    private final SecurityHandler contextRunnable;

    public CrestComandsFactory(ConsoleSession session, SecurityHandler contextRunnable) {
        this.session = session;
        this.contextRunnable = contextRunnable;
    }

    @Override
    public Command create() {
        return new CrestCommands(session, contextRunnable);
    }
}
