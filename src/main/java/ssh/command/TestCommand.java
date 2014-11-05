package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Debug command.
 * @author Dennis
 *
 */
public class TestCommand extends PiCommand {

	public TestCommand(List<String> args) {
		super(args);
	}
	
	public TestCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}
	
	@Override
	public void start(Environment env) throws IOException {
		System.out.println("Test Command Received!");
		result += "Test Answer";
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
