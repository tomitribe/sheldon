package org.tomitribe.ssh.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.tomitribe.telnet.impl.ConsoleSession;
import org.tomitribe.telnet.impl.StopException;
import org.tomitribe.telnet.impl.TtyCodes;

public class TomEECommands implements Command, Runnable, TtyCodes {

	private OutputStream err;
	private ExitCallback cbk;
	private InputStream in;
	private OutputStream out;
	private Environment env;
	private Thread thread;
	
	private final String prompt;

	public TomEECommands(String prompt) {
		super();
		this.prompt = prompt;
	}

	@Override
	public void destroy() {
		thread.interrupt();
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback cbk) {
		this.cbk = cbk;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void start(Environment env) throws IOException {
		this.env = env;
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			new ConsoleSession(prompt).doSession(in, out, true);
		} catch (StopException s) {
            // exit normally
        } catch (Throwable t) {
            t.printStackTrace();
        }
		
		cbk.onExit(0);
	}
}
