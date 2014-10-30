package files;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import global.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.sql.SQLException;
import java.util.List;

import ssh.sftp.PiFileSystemFactory;
import db.UserStatementMaker;

public class FolderMonitor implements Runnable {

	public static void initialiseMonitors() {
		PiFileSystemFactory.init();
		try {
			List<String> names = UserStatementMaker.getUserNameList();
			names.forEach(n -> new Thread(new FolderMonitor(n)).start());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private WatchService service;

	private FolderMonitor(String user) {
		new File(PiFileSystemFactory.homeDirs.get(user));
		try {
			service = FileSystems.getDefault().newWatchService();
			new File(PiFileSystemFactory.homeDirs.get(user))
					.toPath().register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE, OVERFLOW);
		} catch (IOException e) {
			Logger.logError("Unable to create WatchService for " + user + ":");
			Logger.logError(e);
		}
	}

	@Override
	public void run() {

	}

}
