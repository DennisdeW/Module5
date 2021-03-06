package ssh.sftp;

import global.Logger;

import java.io.File;

import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.common.file.nativefs.NativeSshFile;

/**
 * This is exactly the same as a NativeSshFile, except that it creates log entries. Not used.
 * @author Dennis
 *
 */
public class PiFile extends NativeSshFile {

	protected PiFile(NativeFileSystemView nativeFileSystemView,
			String fileName, File file, String userName) {
		super(nativeFileSystemView, fileName, file, userName);
		Logger.log("Created file: name=" + fileName + " path=" + file.getAbsolutePath());
	}

}
