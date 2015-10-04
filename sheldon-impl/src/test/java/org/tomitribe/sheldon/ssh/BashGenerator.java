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

import com.sun.codemodel.JExpr;
import org.apache.openejb.loader.IO;
import org.tomitribe.util.Files;
import org.tomitribe.util.Join;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * Bash required to use this class.
 *
 * Purpose of this class is to generate and execute bash scripts
 * in support of creating "bash does it this way" test cases.
 *
 * The goal of the argument parsing in Sheldon is to be 100% bash compatible
 *
 * Some manual setup is required to
 */
public class BashGenerator {

    private static final Class<WriteArgs> MAIN = WriteArgs.class;

    private final File base;

    public BashGenerator(final File base) throws IOException {
        this.base = base;

        // setup
        final String className = MAIN.getName();
        final String classFile = className.replace('.', '/') + ".class";
        final InputStream stream = this.getClass().getResourceAsStream("/" + classFile);

        final File file = new File(base, classFile);
        Files.mkparent(file);
        IO.copy(stream, file);
    }

    public static void main(String[] args) throws Exception {
        final BashGenerator bashGenerator = new BashGenerator(new File("/tmp/fun"));
//        bashGenerator.generateAsserts();
        int i = 0;
        bashGenerator.generate("test", i++, "\"\" '' \"\"'' ''\"\" ");
        bashGenerator.generate("test", i++, "\"\" '' \"\"a'' ''\"\" ");
        bashGenerator.generate("test", i++, "\"\" '' a\"\"'' ''\"\" ");
        bashGenerator.generate("test", i++, "\"\" '' \"\"''a ''\"\" ");
    }

    public void generate(final String prefix, int i, final String argumentline) throws IOException, InterruptedException, ClassNotFoundException {
        final String outputFilePath = new File(base, prefix + "-" + i).getAbsolutePath();
        final File script = new File(outputFilePath + ".sh");
        final File ser = new File(outputFilePath + ".ser");

        final PrintStream out = new PrintStream(script);
        out.print("!/bin/bash\n");
        out.printf("export OUTPUT_FILE=\"%s\"\n", ser.getAbsolutePath());
        out.printf("java -cp %s %s %s\n", base.getAbsolutePath(), MAIN.getName(), argumentline);
        out.flush();
        out.close();

        assertTrue(script.setExecutable(true));

        final ProcessBuilder builder = new ProcessBuilder(script.getAbsolutePath());
        final Process process = builder.start();
        process.waitFor();

        if (ser.exists()) {
            final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(ser));
            final String[] strings = (String[]) objectInputStream.readObject();
            final String join = Join.join(", ", BashGenerator::quote, strings);

            System.out.printf("assertArguments(%s, %s);%n", quote(argumentline), join);
        } else {
            System.out.printf("assertInvalidArguments(%s);%n", quote(argumentline));
        }
    }

    private static String quote(final String string) {
        return JExpr.quotify('"', string);
    }

    public void generateAsserts() throws IOException, InterruptedException, ClassNotFoundException {
        for (int i = 0; i < 256; i++) {
            generate("bare", i, "one" + ((char) i) + "two   three");
        }
        for (int i = 0; i < 256; i++) {
            generate("bare-escaped", i, "one\\" + ((char) i) + "two   three");
        }
        for (int i = 0; i < 256; i++) {
            generate("singlequoted", i, "\'one" + ((char) i) + "two\'   three");
        }
        for (int i = 0; i < 256; i++) {
            generate("singlequoted-escaped", i, "\'one\\" + ((char) i) + "two\'   three");
        }
        for (int i = 0; i < 256; i++) {
            generate("doublequoted", i, "\"one" + ((char) i) + "two\"   three");
        }
        for (int i = 0; i < 256; i++) {
            generate("doublequoted-escaped", i, "\"one\\" + ((char) i) + "two\"   three");
        }
    }
}
