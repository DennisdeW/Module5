package ssh.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.UserStatementMaker;

public class CheckUserCommand extends PiCommand {

	private String name;
	
	public CheckUserCommand(List<String> args, InputStream in,
			OutputStream out, OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		name = args.get(1);
	}

	@Override
	public void start(Environment env) throws IOException {
		try {
			boolean res = UserStatementMaker.accountExists(name);
			result += res+"";
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.CHECK_USER;
	}

}
