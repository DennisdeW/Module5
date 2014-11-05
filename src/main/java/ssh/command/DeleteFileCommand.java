package ssh.command;

import files.FileDescriptor;
import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;

public class DeleteFileCommand extends PiCommand {

	private String user;
	private String filename;

	public DeleteFileCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		user = args.get(1);
		filename = args.get(2);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun(user)) {
			result = "false";
			return;
		}
		try {
			int uid = UserStatementMaker.getId(user);
			Set<FileDescriptor> owned = FileStatementMaker.getOwnedFiles(uid);
			FileDescriptor target = owned
					.stream()
					.filter(fd -> fd.getIdentifier()
							.equals(filename.getBytes())).findFirst()
					.orElse(null);
			if (target == null) {
				result = "false";
				return;
			}
			result = FileStatementMaker.deleteDescriptor(target) ? "true" : "false";
		} catch (SQLException | UnknownUserException e) {
			Logger.logError(e);
			result = "false";
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DELETE_FILE;
	}

}
