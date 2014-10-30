package ssh;

import org.apache.sshd.server.ExitCallback;

/**
 * Doesn't do anything. Even Apache MINA staff don't use the callbacks...
 * @author Dennis
 *
 */
public class DefaultExitCallback implements ExitCallback {

	@Override
	public void onExit(int exitValue) {
	}

	@Override
	public void onExit(int exitValue, String exitMessage) {

	}

}
