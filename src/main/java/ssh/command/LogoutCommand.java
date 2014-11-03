package ssh.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class LogoutCommand extends PiCommand {

	private String name;
	
	public LogoutCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		name = args.get(1);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun(name)) {
			result += "Failure: not logged in";
			return;
		}
		PiSession.logOut(name);
		result += "Logged out.";
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
