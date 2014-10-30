package ssh.command;

import global.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;

import db.UnknownUserException;
import db.UserStatementMaker;

public class CreateUserCommand extends PiCommand {

	public CreateUserCommand(List<String> args) {
		super(args);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun())
			return;
		try {
			UserStatementMaker.createAccount(args[1], args[2].getBytes());
			Logger.log("New user account created: " + args[1] + " -- id="
					+ UserStatementMaker.getId(args[1]));
		} catch (SQLException | UnknownUserException e) {
			Logger.logError("Failed to create user: " + e);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.CREATE_USER;
	}

}
