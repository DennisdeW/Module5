package files;

import java.io.File;

public interface Crypto {

	File decrypt(byte[] cipher);
	File encrypt(byte[] plain);
	
}
