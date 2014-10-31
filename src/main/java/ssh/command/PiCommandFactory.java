package ssh.command;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.ExitCallback;

import ssh.DefaultExitCallback;
import ssh.command.PiCommand.PiCommandType;

/**
 * Factory which causes commands to be processed by the PiCommandType enum.
 * 
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
		this.exit = new DefaultExitCallback();
	}

	public PiCommandFactory() {
		this.in = System.in;
		this.out = System.out;
		this.err = System.err;
		this.exit = new DefaultExitCallback();
	}

	@Override
	public Command createCommand(String command) {
		if (command.contains(";")) {
			List<PiCommand> commands = new ArrayList<>();
			for (String c : command.split(";")) {
				commands.add(PiCommandType.getCommand(c, in, out, err, exit));
			}
			return new CompoundCommand(commands, in, out, err, exit);
		}
		return PiCommandType.getCommand(command, in, out, err, exit);
	}

}
