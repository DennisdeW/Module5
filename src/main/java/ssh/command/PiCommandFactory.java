package ssh.command;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.ExitCallback;

import ssh.command.PiCommand.PiCommandType;

/**
 * Factory which causes commands to be processed by the PiCommandType enum.
 * @author Dennis
 *
 */
public class PiCommandFactory implements CommandFactory {
	private final InputStream in;
	private final OutputStream out, err;
	private final ExitCallback exit;

	public PiCommandFactory(InputStream in, OutputStream out, OutputStream err,
			ExitCallback exit) {
		this.in = in;
		this.out = out;
		this.err = err;
		this.exit = exit;
	}

	public PiCommandFactory() {
		this.in = null;
		this.out = null;
		this.err = null;
		this.exit = null;
	}

	@Override
	public Command createCommand(String command) {
		if (in != null)
			return PiCommandType.getCommand(command, in, out, err, exit);
		return PiCommandType.getCommand(command);
	}

}
