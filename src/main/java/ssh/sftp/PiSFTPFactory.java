package ssh.sftp;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.sftp.SftpSubsystem;

import ssh.SSHManager;

/**
 * Sets up the SFTP subsystem with the correct FileSystemFactory.
 * 
 * @author Dennis
 *
 */
public class PiSFTPFactory implements NamedFactory<Command> {

	@Override
	public Command create() {
		SftpSubsystem sftp = (SftpSubsystem) new PiSFTPSubsystem.Factory()
				.create();
		sftp.setFileSystemView(PiFileSystemFactory.INSTANCE
				.createFileSystemView(SSHManager.session));
		return sftp;
	}

	@Override
	public String getName() {
		return "pi-sftp";
	}

}
