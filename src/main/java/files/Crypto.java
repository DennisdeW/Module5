package files;

import java.io.File;

public interface Crypto {

	File encrypt(File plain);
	File decrypt(File cipher);
	
}
