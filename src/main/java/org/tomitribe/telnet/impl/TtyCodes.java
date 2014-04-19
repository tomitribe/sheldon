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

/**
 * @version $Revision$ $Date$
 */
public interface TtyCodes {

    char ESC = (char) 27;

    String TTY_RESET = ESC + "[0m";

    String TTY_BRIGHT = ESC + "[1m";

    String TTY_DIM = ESC + "[2m";

    String TTY_UNDERSCORE = ESC + "[4m";

    String TTY_BLINK = ESC + "[5m";

    String TTY_REVERSE = ESC + "[7m";

    String TTY_HIDDEN = ESC + "[8m";

    /* Foreground Colors */

    String TTY_FG_BLACK = ESC + "[30m";

    String TTY_FG_RED = ESC + "[31m";

    String TTY_FG_GREEN = ESC + "[32m";

    String TTY_FG_YELLOW = ESC + "[33m";

    String TTY_FG_BLUE = ESC + "[34m";

    String TTY_FG_MAGENTA = ESC + "[35m";

    String TTY_FG_CYAN = ESC + "[36m";

    String TTY_FG_WHITE = ESC + "[37m";

    /* Background Colors */

    String TTY_BG_BLACK = ESC + "[40m";

    String TTY_BG_RED = ESC + "[41m";

    String TTY_BG_GREEN = ESC + "[42m";

    String TTY_BG_YELLOW = ESC + "[43m";

    String TTY_BG_BLUE = ESC + "[44m";

    String TTY_BG_MAGENTA = ESC + "[45m";

    String TTY_BG_CYAN = ESC + "[46m";

    String TTY_BG_WHITE = ESC + "[47m";

}
