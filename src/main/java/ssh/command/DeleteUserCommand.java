package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.UnknownUserException;
import db.UserStatementMaker;

public class DeleteUserCommand extends PiCommand {

	private int id;
	private String name;
	
	public DeleteUserCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		try {
			id = UserStatementMaker.getId(args.get(1));
			name = args.get(1);
		} catch (SQLException | UnknownUserException e) {
			result += "This account does not exists. ";
		}
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun(name)) {
			result += "Failure: not logged in.";
			return;
		}
		try {
			UserStatementMaker.deleteAccount(id);
			result += "Account deleted.";
		} catch (SQLException e) {
			Logger.logError(e);
			result += "The account could not be deleted.";
		}

	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DELETE_USER;
	}

}
