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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.telnet.impl;

public interface TelnetCodes {

    int SE = 240;

    int NOP = 241;

    int DATA_MARK = 242;

    int BREAK = 243;

    int INTERRUPT_PROCESS = 244;

    int ABORT_OUTPUT = 245;

    int ARE_YOU_THERE = 246;

    int ERASE_CHARACTER = 247;

    int ERASE_LINE = 248;

    int GO_AHEAD = 249;

    int SB = 250;

    int WILL = 251;

    int WONT = 252;

    int DO = 253;

    int DONT = 254;

    int IAC = 255;

}
