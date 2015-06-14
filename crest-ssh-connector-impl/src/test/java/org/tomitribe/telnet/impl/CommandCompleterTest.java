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
package org.tomitribe.telnet.impl;

import java.util.ArrayList;
import java.util.List;

import jline.console.completer.Completer;

import org.junit.Before;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.connector.ssh.CommandCompleter;

import static org.junit.Assert.*;

public class CommandCompleterTest {

    public static final String UNIT_TEST_PROMPT = "unittest>";
    protected Main main;
    protected Completer completer;

    @Before
    public void setUp() {
        main = new Main(EchoCommand.class);
        completer = new CommandCompleter(main);
    }

    @Test
    public void testShouldThrowNullPointerExceptionIfCandidatesIsNull() throws Exception {
        try {
            completer.complete(null, 0, null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testShouldReturnAListOfAllCommandsWhenBufferIsEmpty() throws Exception {
        final List<CharSequence> candidates = new ArrayList<CharSequence>();
        completer.complete("", 0, candidates);

        assertTrue(candidates.contains("echo"));
    }

    @Test
    public void testShouldReturnAListOfAllCommandsWithSamePrefixWhenACoupleOfCharactersPreceedTheCursor()
            throws Exception {
        final List<CharSequence> candidates = new ArrayList<CharSequence>();
        completer.complete("ec", 1, candidates);

        assertTrue(candidates.contains("echo"));
    }

    public static class EchoCommand {

        @Command
        public String echo(String input) {
            return input;
        }
    }
}
