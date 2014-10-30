package ssh.command;

import global.Logger;

import java.io.IOException;
import java.util.List;

import org.apache.sshd.server.Environment;

import ssh.SSHManager;

/**
 * Command to stop the server.
 * @author Dennis
 *
 */
public class StopCommand extends PiCommand {

	public StopCommand(List<String> args) {
		super(args);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun())
			return;
		if (args[1].equals("reallyactuallystop"))
			SSHManager.stop();
		else
			Logger.log("STOP received with wrong password.");
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.STOP;
	}
}