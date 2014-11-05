package ssh.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Dummy command to avoid crashes when an unkown command is sent.
 *
 * @author Dennis
 *
 */
public class DummyCommand extends PiCommand {

	public DummyCommand(List<String> args) {
		super(args);
	}

	public DummyCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		exit.onExit(0);
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DUMMY;
	}

}
