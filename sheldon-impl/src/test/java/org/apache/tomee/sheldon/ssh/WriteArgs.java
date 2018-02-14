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
package org.apache.tomee.sheldon.ssh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This class intentionally has no dependencies on *ANY* other class
 * unless it is a built-in JVM class
 * <p>
 * This class will be moved alone to a temp directory and executed
 * via script to support creating the test asserts.
 * <p>
 * DO NOT add dependencies to other classes, even utilities, or this
 * class will not be executable via that simple script.
 */
public class WriteArgs {
    public static void main(String[] args) throws IOException {
        final String path = System.getenv().get("OUTPUT_FILE");
        final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(path)));
        out.writeObject(args);
        out.close();
    }
}
