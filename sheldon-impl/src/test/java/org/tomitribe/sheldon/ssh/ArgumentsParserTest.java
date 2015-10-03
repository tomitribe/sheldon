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
package org.tomitribe.sheldon.ssh;

import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.tomitribe.sheldon.ssh.ArgumentAssertions.assertArguments;
import static org.tomitribe.sheldon.ssh.ArgumentsParser.parse;

public class ArgumentsParserTest {

    @Test
    public void single() {
        assertArguments("a", "a");
    }

    @Test
    public void simple() {
        assertEquals(asList("a", "b", "c", "1234", "word"), asList(parse("a b c 1234 word")[0].get()));
        assertEquals(asList("a", "b", "-c", "--1234", "word"), asList(parse("a b -c --1234 word")[0].get()));
    }

    @Test
    public void quotes() {
        assertArguments("a \"b\" c \"1234,76\" \\\\ \"sentence\\ \\\\\\\" chkdwc\"", "a", "b", "c", "1234,76", "\\", "sentence \\\" chkdwc");
    }

    @Test
    public void singleQuote() {
        assertArguments("a 'b' c '1234,76' \\\\ \"sentence\\ \\\\' chkdwc\"", "a", "b", "c", "1234,76", "\\", "sentence \\' chkdwc");
        assertArguments("a 'b' c '1234,76' \\\\ 'sentence\\ \\\\\\' chkdwc'", "a", "b", "c", "1234,76", "\\", "sentence \\' chkdwc");
    }

    @Test
    public void piping() {
        {
            final Arguments[] args = parse("a|b");
            assertEquals(2, args.length);
            assertEquals(singletonList("a"), asList(args[0].get()));
            assertEquals(singletonList("b"), asList(args[1].get()));
        }
        {
            final Arguments[] args = parse("test --option=value \"-quoted=ckdwc\\\\cekwcbw\" | grep \"==foo\"");
            assertEquals(2, args.length);
            assertEquals(asList("test", "--option=value", "-quoted=ckdwc\\cekwcbw"), asList(args[0].get()));
            assertEquals(asList("grep", "==foo"), asList(args[1].get()));
        }
    }


    /**
     * An '\' in quotes should be treated as a plain '\' character
     */
    @Test
    @Ignore
    public void escapesInQuotes() {
        assertArguments("one two", "one", "two");
        assertArguments("one\\ two", "one two");
        assertArguments("\"one two\"", "one two");
        assertArguments("\"one\\ two\"", "one\\ two");
    }
}