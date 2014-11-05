package ssh.command;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import net.PiSession;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;

public class CheckFileCommand extends PiCommand {

	private String fileid;

	public CheckFileCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		fileid = args.get(1);
	}

	@Override
	public void start(Environment env) throws IOException {
		if (PiSession.getUser() != null)
			try {
				int uid = UserStatementMaker.getId(PiSession.getUser());
				boolean ownsFile = FileStatementMaker
						.getOwnedFiles(uid)
						.stream()
						.anyMatch(
								fd -> fd.getIdentifier().equals(fileid)
								&& fd.getOwner() == uid);
				result += ownsFile ? "true" : "false";
			} catch (SQLException | UnknownUserException e) {
				result += "false";
				Logger.logError(e);
			}
		else
			result = "false";
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.CHECK_FILE;
	}

}
