package ssh;

import global.Logger;

import java.io.File;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.InvertedShell;
import org.apache.sshd.server.shell.InvertedShellWrapper;
import org.apache.sshd.server.shell.ProcessShellFactory;

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











import com.sun.jna.Platform;

import ssh.command.PiCommandFactory;
import ssh.sftp.PiFileSystemFactory;
import ssh.sftp.PiSFTPFactory;

public class SSHManager {

	private static final SshServer SSH;
	public static boolean limitedUser = false;
	public static String username = null;
	public static Session session = null;

	static {
		SSH = SshServer.setUpDefaultServer();
		SSH.setPort(20022);
		SSH.setPasswordAuthenticator(new Authenticator());
		//SSH.setShellFactory(new ProcessShellFactory(getShellString()));
		SSH.setCommandFactory(new ScpCommandFactory(new PiCommandFactory()));
		SSH.setHost("localhost");
		SSH.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		//VirtualFileSystemFactory fsf = new VirtualFileSystemFactory();
		//SSH.setFileSystemFactory(fsf);
		SSH.setFileSystemFactory(new PiFileSystemFactory());
		List<NamedFactory<Command>> subSystemList = new ArrayList<>();
		//subSystemList.add(new PiSFTPFactory());
		subSystemList.add(new SftpSubsystem.Factory());
		SSH.setSubsystemFactories(subSystemList);
	}

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

	public static void stop() {
		Logger.log("Stopping SSH server.");
		try {
			SSH.stop();
		} catch (InterruptedException e) {
			Logger.logError(e);
			e.printStackTrace();
		}
		Logger.log("SSH stopped.");
	}

	public static void main(String[] args) throws IOException {
		Logger.init();
		//Runtime.getRuntime().exec("C:\\Users\\Dennis\\Desktop\\putty.exe");
		start();
		try {
			Thread.sleep(180000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stop();
	}

	
	@SuppressWarnings("unused")
	private static String[] getShellString() {
		if (Platform.isWindows())
			return new String[] { "C:\\Windows\\SysWOW64\\cmd",
					"/K cd " + new File("").getAbsolutePath() + "\\sandbox" };
		else
			return new String[] { "/bin/sh" };
	}
}
