package ssh.command;

import global.PiCloudConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import net.PiSession;
import net.packets.DataPacket;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import db.FileStatementMaker;
import db.UnknownUserException;
import db.UserStatementMaker;

public class DownloadCommand extends PiCommand {

	private String fileid;
	public static DataPacket packet = null;

	public DownloadCommand(List<String> args, InputStream in, OutputStream out,
			OutputStream err, ExitCallback exit) {
		super(args, in, out, err, exit);
		fileid = args.get(1);
		packet = null;
	}

	@Override
	public void start(Environment env) throws IOException {
		try {
			int uid = UserStatementMaker.getId(PiSession.getUser());
			boolean ownsFile = FileStatementMaker
					.getOwnedFiles(uid)
					.stream()
					.anyMatch(
							fd -> fd.getIdentifier().equals(fileid)
							&& fd.getOwner() == uid);
			if (ownsFile) {
				File target = new File("storage/" + fileid);
				FileInputStream fis = new FileInputStream(target);
				byte[] raw = new byte[(int) target.length()];
				fis.read(raw);
				fis.close();
				File decrypted = PiCloudConstants.CRYPTO_IMPL.decrypt(raw);
				fis = new FileInputStream(decrypted);
				raw = new byte[(int) target.length()];
				fis.read(raw);
				fis.close();
				decrypted.delete();
				packet = DataPacket.create(raw);
				result += "true";
			} else {
				result += "false";
				packet = null;
			}
		} catch (SQLException | UnknownUserException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public PiCommandType getType() {
		return PiCommandType.DOWNLOAD;
	}

}
