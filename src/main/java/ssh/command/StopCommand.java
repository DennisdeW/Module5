package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.PiServer;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

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
	
	public StopCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun(args[1]))
			return;
		if (args[2].equals("reallyactuallystop"))
			PiServer.stopServer();
		else
			Logger.log("STOP received with wrong password.");
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