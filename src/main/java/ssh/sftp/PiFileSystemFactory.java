package ssh.sftp;

import global.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;

import ssh.SSHManager;

import com.sun.jna.Platform;

import db.UserStatementMaker;

public class PiFileSystemFactory extends VirtualFileSystemFactory {

	public static Map<String, String> homeDirs;

	static {
		new PiFileSystemFactory().setUserDirs();
	}

	/*
	 * @Override protected String computeRootDir(String userName) { File f = new
	 * File(new File("").getAbsolutePath() + File.separator + "storage" +
	 * File.separator + userName); f.mkdirs(); return new
	 * File("").getAbsolutePath() + File.separator + "storage" + File.separator
	 * + userName; }
	 */
	public PiFileSystemFactory() {
		super();
		setUserDirs();
	}

	private void setUserDirs() {
		homeDirs = new HashMap<>();
		List<String> names = null;
		String base = new File("").getAbsolutePath();
		try {
			names = UserStatementMaker.getUserNameList();
		} catch (SQLException e) {
			Logger.logError(e);
			e.printStackTrace();
		}
		names.forEach(n -> {
			setUserHomeDir(n, base + "/storage/" + n);
			homeDirs.put(n, base + "/storage/" + n);
			if (new File(base + "/storage/" + n).mkdirs())
				Logger.log("Created new User Storage Folder for " + n);
		});
		setUserHomeDir("Guest", base + "/storage/Guest");
		homeDirs.put("Guest", base + "/storage/Guest");
		if (new File(base + "/storage/Guest").mkdirs())
			Logger.log("Created new User Storage Folder for Guest");
		setDefaultHomeDir(new File("").getAbsolutePath() + "/");
	}

	public static void init() {
	}
}
