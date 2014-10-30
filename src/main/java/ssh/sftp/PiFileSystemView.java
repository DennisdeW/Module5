package ssh.sftp;

import global.Logger;

import java.io.File;
import java.util.Map;

import org.apache.sshd.common.file.SshFile;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.common.file.nativefs.NativeSshFile;

import ssh.SSHManager;

public class PiFileSystemView extends NativeFileSystemView {

	private Map<String, String> roots;
	
	public PiFileSystemView(String userName, Map<String, String> roots,
			String current, char separator, boolean caseInsensitive) {
		super(userName, roots, current, separator, caseInsensitive);
		this.roots = roots;
		Logger.log("FileSystemView for " + userName + " created at " + current);
	}	
	
	protected SshFile getFile(String dir, String file) {
        // Compute root + non rooted absolute file
        String root = null;
        if (roots.size() > 1 && file.startsWith(File.separator)) {
            file = file.substring(1);
        }
        for (String r : roots.keySet()) {
            if (!file.isEmpty() && r.equals(file + File.separator)) {
                file += File.separator;
            }
            if (file.startsWith(r)) {
                root = r;
                file = File.separator + file.substring(r.length());
                break;
            }
        }
        if (root == null) {
            // file is relative to dir
            file = dir + file;
            for (String r : roots.keySet()) {
                if (file.startsWith(r)) {
                    root = r;
                    file = File.separator + file.substring(r.length());
                    break;
                }
            }
        }
        if (root == null) {
            throw new IllegalStateException("Could not find root dir for file(" + dir + ", " + file + ")");
        }
        // Physical root
        String physicalRoot = roots.get(root);
        // get actual file object
        String physicalName = NativeSshFile.getPhysicalName(physicalRoot, File.separator, file, true);
        File fileObj = new File(physicalName);

        // strip the root directory and return
        String userFileName = root + physicalName.substring(physicalRoot.length());
        return new PiFile(this, userFileName, fileObj, SSHManager.username);
    }

	

}
