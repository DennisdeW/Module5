package files;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import db.UserStatementMaker;
import ssh.sftp.PiFileSystemFactory;

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
	
	private File directory;
	
	private FolderMonitor(String user) {
		directory = new File(PiFileSystemFactory.homeDirs.get(user));
	}
	
	@Override
	public void run() {

	}

}
