package org.tomitribe.ssh.impl;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

public class TomEEShellFactory implements Factory<Command> {

	@Override
	public Command create() {
		return new TomEECommands("prompt>");
	
	}

}
