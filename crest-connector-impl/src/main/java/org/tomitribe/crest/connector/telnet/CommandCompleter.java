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
package org.tomitribe.crest.connector.telnet;

import java.util.List;

import org.tomitribe.crest.Main;

import jline.console.completer.Completer;
import static jline.internal.Preconditions.checkNotNull;

/**
 * JLine completer that inspects the crest commands available along with their sub-commands and arguments and provides a
 * list of candidates
 *
 */
public class CommandCompleter implements Completer {

    private final Main main;

    public CommandCompleter(Main main) {
        this.main = main;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        try {
            checkNotNull(candidates);

            if (buffer == null) {
                buffer = "";
            }

            candidates.addAll(main.complete(buffer, cursor));

        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        final int pos = buffer.lastIndexOf(" ", cursor) + 1;
        return pos;
    }

}
