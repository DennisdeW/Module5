package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.UnknownUserException;
import db.UserStatementMaker;

public class LoginCommand extends PiCommand {

	private String name, pass;

	public LoginCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		name = args.get(1);
		pass = args.get(2);
	}

	@Override
	public void start(Environment env) throws IOException {
		try {
			int id = UserStatementMaker.getId(name);
			byte[] salted = UserStatementMaker.saltPass(id, pass.getBytes());
			byte[] expected = UserStatementMaker.getPass(id);
			boolean authentic = Arrays.equals(salted, expected);
			result += authentic;
			if (authentic)
				PiSession.logIn(name);
		} catch (SQLException | UnknownUserException e) {
			Logger.logError(e);
			result += "Error during login...";
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.LOGIN;
	}

}
