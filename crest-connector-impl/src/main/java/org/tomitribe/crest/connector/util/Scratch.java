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
package org.tomitribe.crest.connector.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Scratch {


    public static void main(String[] args) throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final ByteArrayOutputStream toOrangeBuffer = new ByteArrayOutputStream();


        final PrintStream toOrange = new PrintStream(toOrangeBuffer);
        final InputStream fromSystemIn = new ByteArrayInputStream(new byte[0]);

        final Future<?> green = executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 99; i > 0; i--) {
                    toOrange.printf("%s bottles of beer on the wall%n", i);
                    System.out.printf("==%s bottles of beer on the wall%n", i);
                }
            }
        });

        green.get();
        toOrange.close();

        final PrintStream toSystemOut = System.out;
        final InputStream fromGreen = new ByteArrayInputStream(toOrangeBuffer.toByteArray());

        final Future<?> orange = executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(fromGreen));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("0")) {
                            toSystemOut.println(line + "!!!!!");
                        } else {
                            toSystemOut.println(line);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });

        orange.get();
    }

}
