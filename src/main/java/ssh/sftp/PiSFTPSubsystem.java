package ssh.sftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.sftp.SftpSubsystem;

public class PiSFTPSubsystem extends SftpSubsystem {

	public void doPut(String arg) throws IOException {
		File f = new File(arg);
		byte[] data = new byte[(int) f.length()];
		FileInputStream fis = new FileInputStream(f);
		fis.read(data);
		fis.close();
		
		Buffer buf = new Buffer(data.length + 9);
		buf.putInt(-1); 						//Total Length
		buf.putByte((byte) SSH_FXP_WRITE); 		//Type
		buf.putInt(new Random().nextInt());		//Id
		buf.putString(arg);						//Handle
		
	}
	
	@Override
	protected void process(Buffer buf) {
		
	}
	
	private void done(int id) throws IOException {
		sendStatus(id, SSH_FX_OK, "");
	}
	
}
