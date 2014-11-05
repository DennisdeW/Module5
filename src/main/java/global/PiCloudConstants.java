package global;

import com.sun.jna.Platform;

import soc.controller.SocManager;
import files.Crypto;
import files.NullCrypto;

public class PiCloudConstants {
	public static final int MAX_FILE_SIZE = 52428800; // 50MiB
	public static final int TEMP_FILE_DURATION = 30;
	public static final int MAX_USER_SIZE = MAX_FILE_SIZE * 5; // 250MiB
	public static final Crypto CRYPTO_IMPL = Platform.isWindows() ? new NullCrypto()
			: SocManager.instance;
}
