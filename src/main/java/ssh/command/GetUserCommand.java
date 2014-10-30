package ssh.command;

import java.io.IOException;
import java.util.List;

import org.apache.sshd.server.Environment;

import db.UserStatementMaker;

/**
 * Debug command to get a user's id number. The number will be printed in the console.
 * @author Dennis
 *
 */
public class GetUserCommand extends PiCommand {
	
	/**
	 * Needs a single argument: A user name.
	 * @param args A list containing the sole argument. 
	 */
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
