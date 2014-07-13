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

package org.tomitribe.commands.factories;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.tomitribe.commands.TomEECommands;
import org.tomitribe.telnet.adapter.SecurityHandler;
import org.tomitribe.telnet.impl.ConsoleSession;

public class TomEEComandsFactory implements Factory<Command> {

    private final ConsoleSession session;
    private final SecurityHandler contextRunnable;

    public TomEEComandsFactory(ConsoleSession session, SecurityHandler contextRunnable) {
        this.session = session;
        this.contextRunnable = contextRunnable;
    }

    @Override
    public Command create() {
        return new TomEECommands(session, contextRunnable);
    }
}
