package ssh.command;

import files.FileSystemManager;
import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import ssh.sftp.PiFileSystemFactory;
import db.UnknownUserException;
import db.UserStatementMaker;

/**
 * Command to create a new user.
 * @author Dennis
 *
 */
public class CreateUserCommand extends PiCommand {

	/**
	 * Needs three arguments:<br>
	 * -The command name (by default) <br>
	 * -The new user's name.<br>
	 * -The new user's password.<br> 
	 * @param args A list of the above arguments.
	 */
	public CreateUserCommand(List<String> args) {
		super(args);
	}

	public CreateUserCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}
	
	/**
	 * Runs the command.
	 */
	public void start(Environment env) throws IOException {
		if (!canRun())
			return;
		try {
			UserStatementMaker.createAccount(args[1], args[2].getBytes());
			Logger.log("New user account created: " + args[1] + " -- id="
					+ UserStatementMaker.getId(args[1]));
			PiFileSystemFactory.register(args[1]);
			FileSystemManager.register(args[1]);
		} catch (SQLException | UnknownUserException e) {
			Logger.logError("Failed to create user: " + e);
		} finally {
			exit.onExit(0);
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.CREATE_USER;
	}

}
