package ssh.sftp;

import global.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;

import db.UserStatementMaker;

/**
 * Creates, sets and maintains the virtual file systems the users can access.<br>
 * These directories are essentially chroot'ed: they will appear to the user as
 * '/', but will actually be in another location, specified by the user's name.
 * 
 * @author Dennis
 *
 */
public class PiFileSystemFactory extends VirtualFileSystemFactory {

	/**
	 * The absolute locations of the users home directories.
	 */
	public static Map<String, String> homeDirs;
	public static final PiFileSystemFactory INSTANCE;

	static {
		INSTANCE = new PiFileSystemFactory();
		INSTANCE.setUserDirs();
	}

	private PiFileSystemFactory() {
		super();
		setUserDirs(); // Shouldn't be necessary as the initializer already
						// calls this, but the server crashes without it...
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
			String sandbox = base + "/storage/" + n;
			setUserHomeDir(n, sandbox);
			homeDirs.put(n, sandbox);
			if (new File(sandbox).mkdirs())
				Logger.log("Created new User Storage Folder for " + n + " at " + sandbox);
			else
				Logger.log("Opened existing sandbox for " + n + " at " + sandbox);
		});
		setUserHomeDir("Guest", base + "/storage/Guest");
		homeDirs.put("Guest", base + "/storage/Guest");
		if (new File(base + "/storage/Guest").mkdirs())
			Logger.log("Created new User Storage Folder for Guest");
		else
			Logger.log("Opened existing sandbox for Guest");
		setDefaultHomeDir(new File("").getAbsolutePath() + "/");
	}

	/**
	 * Does nothing, but ensures that the initializer is run.
	 */
	public static void init() {
		Logger.log("Starting PiFileSystemFactory...");
	}

	public static void register(String user) {
		String sandbox = new File("").getAbsolutePath() + "/storage/" + user;
		new File(sandbox).mkdirs();
		Logger.log("Created new folder for " + user + " at " + sandbox);
		INSTANCE.setUserHomeDir(user, sandbox);
		homeDirs.put(user, sandbox);
	}
}
