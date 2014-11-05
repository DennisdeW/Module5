package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.PiServer;
import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Command to stop the server.
 *
 * @author Dennis
 *
 */
public class StopCommand extends PiCommand {

	public StopCommand(List<String> args) {
		super(args);
	}

	public StopCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (PiSession.getUser() != null)
			if (args[1].equals("reallyactuallystop")) {
				PiServer.stopServer();
				result += "true";
			} else {
				Logger.log("STOP received with wrong password.");
				result += "false";
			}
		else
			result = "false";
		exit.onExit(0);
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.STOP;
	}
}