package ssh;

import global.Logger;

import org.apache.sshd.common.SshException;
import org.apache.sshd.server.ExitCallback;

import ssh.command.PiCommand;

/**
 * Terminates the session
 * 
 * @author Dennis
 *
 */
public class DefaultExitCallback implements ExitCallback {

	@Override
	public void onExit(int exitValue) {
		if (PiCommand.terminateSession) {
			Logger.log("Terminating Session");
			try {
				SSHManager.session.exceptionCaught(new SshException(
						"not actually an exception"));
			} catch (Throwable t) {
			}
		} else
			Logger.log("Ignoring terminate command");
	}

	@Override
	public void onExit(int exitValue, String exitMessage) {
		onExit(exitValue);
	}

}
