package ssh.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.sshd.server.Environment;

import db.UnknownUserException;
import db.UserStatementMaker;

public class GetUserCommand extends PiCommand {
	
	public GetUserCommand(List<String> args) {
		super(args);
		System.out.println("GetUserCommand.GetUserCommand()");
	}

	@Override
	public void start(Environment env) throws IOException {
		if (!canRun())
			return;
		System.out.println("GetUserCommand.start()");
		for (String arg : args)
			System.out.println(arg);
		try {
			System.out.println(UserStatementMaker.getId(args[1]));
			env.getEnv().get(Environment.ENV_TERM);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.GET_USER;
	}

}
