package ssh.command;

import global.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;

import ssh.SSHManager;

/**
 * Base class for commands.
 * 
 * @author Dennis
 *
 */
public abstract class PiCommand implements Command {

	public static boolean terminateSession = true;

	protected InputStream in;
	protected OutputStream out, err;
	protected ExitCallback exit;
	protected String[] args;

	public PiCommand(List<String> args) {
		this.args = args.toArray(new String[] {});
		try {
			throw new UnsupportedOperationException();
		} catch (UnsupportedOperationException e) {
			Logger.logError(e);
		}
	}

	protected boolean canRun() {
		return !SSHManager.limitedUser
				|| getType() == PiCommandType.CREATE_USER;
	}

	public PiCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		this.args = args.toArray(new String[] {});
		this.in = in;
		this.out = out;
		this.err = err;
		this.exit = exit;
	}

	public abstract PiCommandType getType();

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.exit = callback;
	}

	/**
	 * Enum which contains all implemented commands.
	 * 
	 * @author Dennis
	 *
	 */
	public enum PiCommandType {
		TEST("test", TestCommand.class), GET_USER("user", GetUserCommand.class), CREATE_USER(
				"create", CreateUserCommand.class), STOP("stop",
				StopCommand.class), DECRYPT("decrypt", DecryptCommand.class), DUMMY(
				"", DummyCommand.class);

		private String command;
		private Class<? extends PiCommand> clazz;

		/**
		 * @param command
		 *            The string to recognize this command by.
		 * @param clazz
		 *            The underlying class for this command.
		 */
		PiCommandType(String command, Class<? extends PiCommand> clazz) {
			this.command = command;
			this.clazz = clazz;
		}

		private PiCommand _getCommand(String args) {
			System.out.println("PiCommand.PiCommandType.getCommand()");
			PiCommand comm = null;
			try {
				comm = clazz.getConstructor(List.class).newInstance(
						Arrays.asList(args.split("-")));
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				Logger.logError("Could not run standard command: " + e);
				e.printStackTrace();
			}
			return comm;
		}

		private PiCommand _getCommand(String args, InputStream in,
				OutputStream out, OutputStream err, ExitCallback exit) {
			System.out.println("PiCommand.PiCommandType.getCommand()");
			PiCommand comm = null;
			try {
				comm = clazz.getConstructor(List.class, InputStream.class,
						OutputStream.class, OutputStream.class,
						ExitCallback.class).newInstance(
						Arrays.asList(args.split("-")), in, out, err, exit);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e2) {
				Logger.logError("Could not run standard command: " + e2);
				e2.printStackTrace();

			}
			return comm;
		}

		/**
		 * Get the enum constant backing the given command string.
		 * 
		 * @param command
		 *            The command to search for, <b>without arguments</b>
		 * @return The enum constant which has the class for this command, or
		 *         DUMMY when the command is not recognized.
		 */
		public static PiCommandType typeOf(String command) {
			for (PiCommandType type : values())
				if (type.command.equalsIgnoreCase(command))
					return type;
			Logger.logError("Unrecognized command: " + command);
			return DUMMY;
		}

		/**
		 * Gets the command which is bound to the given string.<br>
		 * Commands should be structured as follows:<br>
		 * <i>command-arg1-arg2-...-argN</i>
		 * 
		 * @param command
		 *            The command with arguments, as above.
		 * @return The command, supplied with the arguments.
		 */
		public static PiCommand getCommand(String command) {
			if (command.startsWith("create"))
				Logger.log("Received Create User Command: "
						+ command.split("-")[1]);
			else
				Logger.log("Received Command: " + command + " (sender="
						+ SSHManager.username + ")");
			renameThread();
			PiCommandType type = null;
			try {
				type = typeOf(command.split("-")[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (type == null)
				throw new IllegalArgumentException("Command \"" + command
						+ "\" is not implemented!");
			return type._getCommand(command);
		}

		public static PiCommand getCommand(String command, InputStream in,
				OutputStream out, OutputStream err, ExitCallback exit) {
			PiCommandType type = null;
			try {
				type = typeOf(command.split("-")[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (type == null)
				throw new IllegalArgumentException("Command \"" + command
						+ "\" is not implemented!");
			return type._getCommand(command, in, out, err, exit);
		}

		private static void renameThread() {
			String cur = Thread.currentThread().getName();
			if (cur.contains("sshd")) {
				char lastchar = cur.charAt(cur.length() - 1);
				Thread.currentThread().setName("SSH Command " + lastchar);
			}
		}
	}
}
