package soc.controller;

import global.Timer;

import java.util.Arrays;

import org.junit.Assert;

import soc.model.GPIOSettings;
import soc.model.Message;

public class SocManagerTest {

	/**
	 * Soc manager test to be used for manual testing.<br>
	 * The default GPIO settings will be used.
	 *
	 * @param sendSpeed
	 *            SPI speed in bits/sec
	 * @param packetSize
	 *            size of a packet, must be 16, 32, 64 or 128 bytes
	 * @param fileSize
	 *            the size in bytes of the whole test file
	 * @param bufferSize
	 *            the size in KB of the receive buffer of the Pi (settings this
	 *            too low will result in losing data)
	 * @param timeOut
	 *            the time-out for every communication action
	 * @param selfTest
	 *            whether to test the Pi sending to itself (MOSI and MISO ports
	 *            connected) or to the DE1
	 */
	public static void testSocManager(int sendSpeed, int packetSize,
			int fileSize, int bufferSize, int timeOut, boolean selfTest) {
		// set up manager
		GPIOSettings settings = new GPIOSettings();
		SocManager manager = new SocManager(settings, sendSpeed, bufferSize);
		// generate 1.000.000 bytes of data
		byte[] data = new byte[fileSize];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) (i % Byte.MAX_VALUE);
		Message msg = new Message(data, false, packetSize);
		// check for msg data to be a multiple of packetSize bytes
		Assert.assertEquals("Data length is not a multiple of " + packetSize
				+ " bytes", 0, msg.getData().length % packetSize);
		// test for speed an reliability
		int assumedTime = sendSpeed / 8 / fileSize * 1000 * 2; // ms
		Timer timer = new Timer(assumedTime, true);
		Message encrypted = manager.sendAndReceiveData(msg, true, timeOut,
				selfTest);
		// Assert.assertNotNull("Encryption response from DE1 timed out (>" +
		// timeOut + "ms) or de1 send pin is low",
		// encrypted);
		// Assert.assertFalse("Encryption not done in assumed time (>" +
		// assumedTime + "ms)",
		// timer.hasExpired());
		timer.restart();
		Message decrypted = manager.sendAndReceiveData(encrypted, false,
				timeOut, selfTest);
		// Assert.assertNotNull("Decryption response from DE1 timed out (>" +
		// timeOut + "ms) or de1 send pin is low",
		// encrypted);
		// Assert.assertFalse("Decryption not done in assumed time (>" +
		// assumedTime + "ms)",
		// timer.hasExpired());
		// test for same message after encryption and decryption
		System.out.println(Arrays.toString(msg.getData()));
		System.out.println(Arrays.toString(encrypted.getData()));
		System.out.println(Arrays.toString(decrypted.getData()));
		Assert.assertArrayEquals(
				"Messages not equal after encryption - decryption cycle!",
				msg.getData(), decrypted.getData());
		manager.shutdown();
	}

}
