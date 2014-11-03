package ssh.command.sftp;

import global.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import ssh.command.DummyCommand;
import ssh.command.PiCommand;
import ssh.command.PiCommand.PiCommandType;

public class SFTPCommand extends PiCommand {

	private PiCommand inner;

	public SFTPCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		String command = "";
		for (String arg : args)
			command += arg;
		inner = (PiCommand) SFTPCommandType.getCommand(command, in, out, err, exit);
	}

	@Override
	public void start(Environment env) throws IOException {
		inner.start(env);
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public static enum SFTPCommandType {
		PUT("put", SFTPPut.class), DUMMY("", DummyCommand.class);

		private String command;
		private Class<? extends PiCommand> clazz;

		/**
		 * @param command
		 *            The string to recognize this command by.
		 * @param clazz
		 *            The underlying class for this command.
		 */
		SFTPCommandType(String command, Class<? extends PiCommand> clazz) {
			this.command = command;
			this.clazz = clazz;
		}

		private static SFTPCommandType getType(String command) {
			for (SFTPCommandType sct : values())
				if (sct.command.equalsIgnoreCase(command))
					return sct;
			return DUMMY;
		}

		public static Command getCommand(String command, InputStream in,
				OutputStream out, OutputStream err, ExitCallback exit) {
			renameThread();
			Logger.log("SFTP Command Received: " + command);
			String comm = command.split("-")[1];
			SFTPCommandType sct = getType(comm);
			return sct._GetCommand(command, in, out, err, exit);
		}

		private Command _GetCommand(String command, InputStream in,
				OutputStream out, OutputStream err, ExitCallback exit) {
			PiCommand comm = null;
			try {
				comm = clazz.getConstructor(List.class,
						InputStream.class, OutputStream.class,
						OutputStream.class, ExitCallback.class).newInstance(
						Arrays.asList(command.split("-")), in, out, err, exit);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e2) {
				Logger.logError("Could not run sftp command: " + e2);
				e2.printStackTrace();

			}
			return comm;
		}

		private static void renameThread() {
			String cur = Thread.currentThread().getName();
			if (cur.contains("sshd")) {
				char lastchar = cur.charAt(cur.length() - 1);
				Thread.currentThread().setName("SFTP Command " + lastchar);
			}
		}
	}
}
