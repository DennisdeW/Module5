package ssh.command;

import java.io.IOException;
import java.util.List;

import org.apache.sshd.server.Environment;

/**
 * Dummy command to avoid crashes when an unkown command is sent.
 * @author Dennis
 *
 */
public class DummyCommand extends PiCommand {

	public DummyCommand(List<String> args) {
		super(args);
	}

	@Override
	public void start(Environment env) throws IOException {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DUMMY;
	}

}
