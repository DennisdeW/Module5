package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Wrapper for multiple commands.
 */
public class CompoundCommand extends PiCommand {

	private List<PiCommand> commands;

	public CompoundCommand(List<PiCommand> args) {
		super(new ArrayList<String>());
		commands = args;
	}

	public CompoundCommand(List<PiCommand> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(new ArrayList<String>(), in, out, err, exit);
		commands = args;
	}

	@Override
	public void start(Environment env) throws IOException {
		PiCommand.terminateSession = false;
		Thread.currentThread().setName(
				"CompoundCommand-" + new Random().nextInt(10));
		Logger.log("Running CompoundCommand with " + commands);
		for (PiCommand pic : commands) {
			Logger.log("Running command: " + pic);
			pic.start(env);
			result += pic.result;
		}
		PiCommand.terminateSession = true;

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return null;
	}

}
