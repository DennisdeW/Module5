package ssh.command.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

public class SFTPPut extends SFTPCommand {

	public SFTPPut(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
	}
	
	@Override
	public void start(Environment env) throws IOException {
		
	}

}
