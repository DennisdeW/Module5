package files;

import java.io.File;

public class NullCrypto implements Crypto {

	@Override
	public File encrypt(File plain) {
		plain.renameTo(new File("test/" + plain.hashCode()));
		return plain;
	}

	@Override
	public File decrypt(File cipher) {
		return cipher;
	}

}
