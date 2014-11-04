package files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class NullCrypto implements Crypto {

	@Override
	public File encrypt(byte[] bs) {
		File f = new File(
				"storage/"
						+ (((long) bs.hashCode() << 32) ^ (long) new SecureRandom()
								.nextInt()));
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(bs);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}

	@Override
	public File decrypt(byte[] cipher) {
		return null;
	}

}
