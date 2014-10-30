package ssh;

import global.Logger;

import java.sql.SQLException;
import java.util.Arrays;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import db.UnknownUserException;
import db.UserStatementMaker;

public class Authenticator implements PasswordAuthenticator {

	public static final String DEFAULT_USER = "Guest", DEFAULT_PASS = "Guest";

	@Override
	/**
	 * Checks whether a user has supplied valid credentials
	 */
	public boolean authenticate(String username, String password,
			ServerSession session) {
		renameThread();
		if (username.equals(DEFAULT_USER) && password.equals(DEFAULT_PASS)) {
			SSHManager.limitedUser = true;
			SSHManager.username = DEFAULT_USER;
			SSHManager.session = session;
			Logger.log("Successful Guest login");
			return true;
		}

		boolean valid = false;

		try {
			int id = UserStatementMaker.getId(username);
			byte[] saltedPass = UserStatementMaker.saltPass(id,
					password.getBytes());
			byte[] storedPass = UserStatementMaker.getPass(id);
			valid = Arrays.equals(saltedPass, storedPass);
		} catch (SQLException | UnknownUserException e) {
			Logger.logError("Error during login: " + e);
			return false;
		}

		if (valid) {
			Logger.log("Successful login for " + username);
			SSHManager.limitedUser = false;
			SSHManager.username = username;
			SSHManager.session = session;
		} else
			Logger.log("Login Failed for " + username
					+ " (passwords did not match)");

		return valid;
	}
	
	private static void renameThread() {
		String cur = Thread.currentThread().getName();
		if (cur.contains("sshd")) {
			char lastchar = cur.charAt(cur.length() - 1);
			Thread.currentThread().setName("SSH Session " + lastchar);
		}
	}

}
