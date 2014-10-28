package global;

/**
 * Helper class for globally usable tools.
 * 
 * @author rvemous
 */
public class Tools {
	
	/**
	 * Checks whether all elements within an byte array are equal to the
	 * specified value.
	 * 
	 * @param data to check
	 * @param value to use
	 * @return whether all elements are equal to the value
	 */
	public static boolean allEqualTo(byte[] data, byte value) {
		if (data == null || data.length == 0) {
			return false;
		}
		for (byte b : data) {
			if (b != value) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Waits for the specified time in milliseconds.<br>
	 * It can be interrupted.
	 * 
	 * @param sleepTime time to sleep
	 */
	public static void waitForMs(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {}
	}
	
	/**
	 * Waits for the specified time in nanoseconds.<br>
	 * It cannot be interrupted.
	 * 
	 * @param sleepTime time to sleep
	 */	
	public static void waitForNs(long sleepTime) {
		long currTime = System.nanoTime();
		while (System.nanoTime() - currTime <= sleepTime);
	}
}
