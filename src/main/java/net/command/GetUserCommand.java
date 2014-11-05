package net.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 * Debug command to get a user's id number. The number will be printed in the
 * console.
 *
 * @author Dennis
 *
 */
public class GetUserCommand extends PiCommand {

	/**
	 * Needs a single argument: A user name.
	 *
	 * @param args
	 *            A list containing the sole argument.
	 */
	public GetUserCommand(List<String> args) {
		super(args);
	}

	public GetUserCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		try {
			env.getEnv().get(Environment.ENV_TERM);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			exit.onExit(0);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.GET_USER;
	}

}
