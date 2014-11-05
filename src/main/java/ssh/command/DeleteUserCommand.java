package ssh.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;
import files.FileDescriptor;
import global.Logger;

public class DeleteUserCommand extends PiCommand {

	private int id;

	public DeleteUserCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (PiSession.getUser() != null)
			try {
				id = UserStatementMaker.getId(PiSession.getUser());
				Set<FileDescriptor> files = FileStatementMaker
						.getOwnedFiles(id);
				files.forEach(fd -> {
					File f = new File("storage/" + fd.getIdentifier());
					f.delete();
					try {
						FileStatementMaker.deleteDescriptor(fd);
					} catch (Exception e) {
						return;
					}
				});
				UserStatementMaker.deleteAccount(id);
				PiSession.logOut();
				result += "true";
			} catch (SQLException | UnknownUserException e) {
				Logger.logError(e);
				result += "false";
			}
		else
			result = "false";

	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DELETE_USER;
	}

}
