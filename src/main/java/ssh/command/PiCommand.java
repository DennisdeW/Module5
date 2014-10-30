package ssh.command;

import global.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import ssh.SSHManager;

import java.util.List;

public abstract class PiCommand implements Command {

	protected InputStream in;
	protected OutputStream out, err;
	protected ExitCallback exit;
	protected String[] args;

	public PiCommand(List<String> args) {
		System.out.println("PiCommand.PiCommand()");
		System.out.println(args.toString());
		this.args = args.toArray(new String[] {});
	}

	protected boolean canRun() {
		return !SSHManager.limitedUser
				|| getType() == PiCommandType.CREATE_USER;
	}

	public PiCommand(String[] args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		System.out.println("PiCommand.PiCommand()");
		this.args = args;
		this.in = in;
		this.out = out;
		this.err = err;
		this.exit = exit;
	}

	public abstract PiCommandType getType();

	@Override
	public void setInputStream(InputStream in) {
		System.out.println("PiCommand.setInputStream()");
		this.in = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		System.out.println("PiCommand.setOutputStream()");
		this.out = out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		System.out.println("PiCommand.setErrorStream()");
		this.err = err;
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		System.out.println("PiCommand.setExitCallback()");
		this.exit = callback;
	}

	public enum PiCommandType {
		TEST("test", TestCommand.class), GET_USER("user", GetUserCommand.class), CREATE_USER(
				"create", CreateUserCommand.class), DUMMY("", DummyCommand.class);

		private String command;
		private Class<? extends PiCommand> clazz;

		PiCommandType(String command, Class<? extends PiCommand> clazz) {
			this.command = command;
			this.clazz = clazz;
		}

		public PiCommand _getCommand(String args) {
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

		public PiCommand _getCommand(String args, InputStream in,
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
					| NoSuchMethodException | SecurityException e) {
				try {
					comm = clazz.getConstructor(List.class).newInstance(
							Arrays.asList(args.split("-")));
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e2) {
					Logger.logError("Could not run standard command: " + e
							+ "\n" + e2);
					e.printStackTrace();
				}
			}
			return comm;
		}

		public static PiCommandType typeOf(String command) {
			for (PiCommandType type : values())
				if (type.command.equalsIgnoreCase(command))
					return type;
			return DUMMY;
		}

		public static PiCommand getCommand(String command) {
			System.out.println("PiCommand.PiCommandType.getCommand()");
			Logger.log("Received Command: " + command + " (sender="
					+ SSHManager.username + ")");
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
			System.out.println("PiCommand.PiCommandType.getCommand()");
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
	}
}
