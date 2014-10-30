package ssh;

import global.Logger;

import org.apache.sshd.server.ExitCallback;

public class DefaultExitCallback implements ExitCallback {

	@Override
	public void onExit(int exitValue) {
	}

	@Override
	public void onExit(int exitValue, String exitMessage) {

	}

}
