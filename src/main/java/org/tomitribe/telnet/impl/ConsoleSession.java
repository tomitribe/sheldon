package org.tomitribe.telnet.impl;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import jline.Terminal;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;

import org.tomitribe.crest.Cmd;
import org.tomitribe.crest.CommandFailedException;
import org.tomitribe.crest.Commands;
import org.tomitribe.crest.Environment;
import org.tomitribe.crest.Main;

public class ConsoleSession implements TtyCodes {

	private final Main main = new Main();
	private final String prompt;
	
	public ConsoleSession(String prompt) {
		super();
		this.prompt = prompt;
		
        final Map<String, Cmd> commands = Commands.get(new BuildIn());
        for (Cmd cmd : commands.values()) {
            main.add(cmd);
        }
	}

	public void doSession(InputStream in, OutputStream out, boolean ssh) throws IOException {
		FilterOutputStream fo = new FilterOutputStream(out) {
			@Override
            public void write(final int i) throws IOException {
                super.write(i);

                // workaround for MacOSX!! reset line after CR..
                if (isMac() && i == ConsoleReader.CR.toCharArray()[0]) {
                    super.write(ConsoleReader.RESET_LINE);
                }
            }
		};
		
		Terminal term = ssh ? null : new UnsupportedTerminal();
		ConsoleReader reader = new ConsoleReader(in, fo, term);
		
		reader.setPrompt(prompt);
        PrintWriter writer = new PrintWriter(reader.getOutput());
        writer.println("");
        writer.println("type \'help\' for a list of commands");
	
        String line;
        try {
			while ((line = reader.readLine().trim()) != null) {
				if (line.length() > 0) {
					handleUserInput(line.trim(), in, fo);
				}
			}
		} catch (StopException stop) {
            throw stop;
        } catch (UnsupportedOperationException e) {
            throw new StopException(e);
        } catch (Throwable e) {
            e.printStackTrace(new PrintStream(out));
            throw new StopException(e);
        }
	}

	private void handleUserInput(String commandline, InputStream in, OutputStream out) {
        final String[] args = commandline.split(" +");
        PrintStream ps = new PrintStream(out);
        
        try {
			final Environment env = new ConsoleEnvironment(ps, in);
            main.main(env, args);
        } catch (CommandFailedException e) {
            if (e.getCause() instanceof StopException) {
                throw (StopException) e.getCause();
            }

            ps.println("Command Bean threw an Exception");
            e.printStackTrace(ps);
        } catch (IllegalArgumentException iae) {
        } catch (StopException stop) {
            throw stop;
        } catch (Throwable throwable) {
            throwable.printStackTrace(ps);
        }
	}

	protected boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}

}
