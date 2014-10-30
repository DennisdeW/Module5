package ssh.command;

import java.io.IOException;
import java.util.List;

import org.apache.sshd.server.Environment;

/**
 * Debug command.
 * @author Dennis
 *
 */
public class TestCommand extends PiCommand {

	public TestCommand(List<String> args) {
		super(args);
	}
	
	@Override
	public void start(Environment env) throws IOException {
		if (!canRun())
			return;
		System.out.println("Test Command Received!");
	}

	@Override
	public void destroy() {
		//NOP
	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.TEST;
	}

}
