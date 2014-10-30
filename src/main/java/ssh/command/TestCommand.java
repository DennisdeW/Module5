package ssh.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

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
