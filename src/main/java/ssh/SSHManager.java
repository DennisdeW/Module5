package ssh;

import files.FileSystemManager;
import global.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.forward.DefaultTcpipForwarderFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;

import ssh.command.PiCommandFactory;
import ssh.sftp.PiFileSystemFactory;




/*
 import org.apache.sshd.server.shell.InvertedShell;
 import org.apache.sshd.server.shell.InvertedShellWrapper;
 import org.apache.sshd.common.Factory;
 import org.apache.sshd.common.util.Buffer;
 import org.apache.sshd.server.Command;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 import java.io.FilterInputStream;
 import java.io.FilterOutputStream;
 */

import db.DatabaseManager;

/**
 * Controller for the SSH/SFTP server
 * 
 * @author Dennis
 *
 */
public class SSHManager {

	/**
	 * The server that handles all SSH communication, including SFTP
	 */
	private static final SshServer SSH;

	/**
	 * Indicates whether the logged-in user is the guest account, which can only
	 * be used to create new accounts.
	 */
	public static boolean limitedUser = false;

	/**
	 * The name of the active user
	 */
	public static String username = null;

	/**
	 * The active session
	 */
	public static Session session = null;

	static {
		Thread.currentThread().setName("StartupThread");
		Logger.init();
		FileSystemManager.init();
		DatabaseManager.init();
		SSH = SshServer.setUpDefaultServer();
		SSH.setPort(20022);
		SSH.setPasswordAuthenticator(new Authenticator());
		SSH.setCommandFactory(new ScpCommandFactory(new PiCommandFactory()));
		SSH.setHost("0.0.0.0");
		SSH.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		SSH.setFileSystemFactory(PiFileSystemFactory.INSTANCE);
		SSH.setTcpipForwarderFactory(new DefaultTcpipForwarderFactory());

		// Add the SFTP subsystem
		List<NamedFactory<Command>> subSystemList = new ArrayList<>();
		subSystemList.add(new SftpSubsystem.Factory());
		SSH.setSubsystemFactories(subSystemList);
	}

	/**
	 * Start the server
	 */
	public static void start() {
		Logger.log("Starting SSH server on port 20022.");
		try {
			SSH.start();
		} catch (IOException e) {
			Logger.logError(e);
			System.exit(1);
		}
		Logger.log("SSH started.");
	}

	/**
	 * Stop the server
	 */
	public static void stop() {
		Logger.log("Stop command received; stopping...");
		FileSystemManager.stop();
		Logger.log("Stopping SSH server.");
		try {
			SSH.stop();
		} catch (InterruptedException e) {
			Logger.logError(e);
			e.printStackTrace();
		}
		Logger.log("SSH stopped.");
		Logger.log("Leftover threads: " + Thread.activeCount());
	}

	public static void main(String[] args) throws IOException {
		// Runtime.getRuntime().exec("C:\\Users\\Dennis\\Desktop\\putty.exe");
		start();
	}

}
