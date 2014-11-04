package ssh.command;

import global.PiCloudConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;

public class CheckUploadCommand extends PiCommand {

	public static String lastUser;
	private String user;
	private int filesize;

	public CheckUploadCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		this.user = args.get(1);
		lastUser = user;
		this.filesize = Integer.parseInt(args.get(2));
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun(user)) {
			result = "false";
			return;
		}
		try {
			if (filesize > PiCloudConstants.MAX_FILE_SIZE) {
				result = "false";
				return;
			}
			int currentlyUsed = FileStatementMaker
					.getTotalSpaceForUser(UserStatementMaker.getId(user));
			if (currentlyUsed + filesize > PiCloudConstants.MAX_USER_SIZE)
				result = "false";
			else
				result = "true";
		} catch (SQLException | UnknownUserException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.CHECK_UPLOAD;
	}

}
