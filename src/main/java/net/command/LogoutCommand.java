package net.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class LogoutCommand extends PiCommand {

	public LogoutCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		PiSession.logOut();
		result += "true";
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.LOGOUT;
	}

}
