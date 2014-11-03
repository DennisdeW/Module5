package ssh.command;

import files.TemporaryFile;
import global.Logger;
import global.PiCloudConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import ssh.SSHManager;
import ssh.sftp.PiFileSystemFactory;

/**
 * Decrypts a given file.
 * @author Dennis
 *
 */
public class DecryptCommand extends PiCommand {

	private final String file;
	
	/**
	 * Requires one argument: the file name.
	 * Note that because file names may contain "-", all arguments
	 * after the first (the command name "decrypt") will be amalgamated.
	 * @param args
	 */
	public DecryptCommand(List<String> args) {
		super(args);
		if (args.size() < 1) {
			file = "";
		} else {
			String t = "";
			for (int i = 1; i < args.size(); i++) {
				t += super.args[i];
			}
			file = PiFileSystemFactory.homeDirs.get(SSHManager.username) + "/" + t;
		}
	}

	public DecryptCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		if (args.size() < 1) {
			file = "";
		} else {
			String t = "";
			for (int i = 1; i < args.size(); i++) {
				t += super.args[i];
			}
			file = PiFileSystemFactory.homeDirs.get(SSHManager.username) + "/" + t;
		}
	}
	
	@Override
	public void start(Environment env) throws IOException {
		Logger.log("Attempting Decrypt for " + file);
		File f = new File(file);
		
		if (!f.canRead()) {
			Logger.logError("Cannot read file " + file + "!");
			exit.onExit(0);
			return;
		}
		
		
		//TODO
		TemporaryFile decrypted = decrypt(f);
		Logger.log("Decrypted: " + decrypted.getAbsolutePath());
		exit.onExit(0);
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

	private TemporaryFile decrypt (File in) {
		File res = new File(in.getAbsolutePath() + ".dec");
		try {
			Files.copy(in.toPath(), res.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new TemporaryFile(res.getAbsolutePath(), PiCloudConstants.TEMP_FILE_DURATION);
	}
}
