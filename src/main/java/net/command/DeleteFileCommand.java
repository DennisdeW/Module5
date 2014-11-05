package net.command;

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

public class DeleteFileCommand extends PiCommand {

	private String filename;

	public DeleteFileCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		filename = args.get(1);
	}

	@Override
	public void start(Environment env) throws IOException {
		try {
			int uid = UserStatementMaker.getId(PiSession.getUser());
			Set<FileDescriptor> owned = FileStatementMaker.getOwnedFiles(uid);
			FileDescriptor target = owned.stream()
					.filter(fd -> fd.getIdentifier().equals(filename))
					.findFirst().orElse(null);
			if (target == null) {
				result = "false";
				return;
			}
			File toDelete = new File("storage/" + target.getIdentifier());
			if (!toDelete.canRead()) {
				result = "false";
				return;
			}
			result = FileStatementMaker.deleteDescriptor(target)
					&& toDelete.delete() ? "true" : "false";
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
