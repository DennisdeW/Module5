package ssh.sftp;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.sftp.SftpSubsystem;

import ssh.SSHManager;

public class PiSFTPFactory implements NamedFactory<Command> {

	@Override
	public Command create() {
		SftpSubsystem sftp = (SftpSubsystem) new SftpSubsystem.Factory()
				.create();
		sftp.setFileSystemView(new PiFileSystemFactory()
				.createFileSystemView(SSHManager.session));
		return sftp;
	}

	@Override
	public String getName() {
		return "pi-sftp";
	}

}
