package soc;

import java.util.Arrays;

import global.Timer;

import org.junit.Assert;
import org.junit.Test;

import com.sun.jna.Platform;

import soc.controller.SocManager;
import soc.model.GPIOSettings;
import soc.model.Message;

public class SocManagerTest {

	@Test
	public void testSocManager() {
		if (!Platform.isLinux()) {
			return;
		}
		// set up manager
		GPIOSettings settings = new GPIOSettings();
		SocManager manager = new SocManager(settings, 1000000, 1000);
		// generate 1.000.000 bytes of data
		byte[] data = new byte[1000000];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i % Byte.MAX_VALUE);
		}
		Message msg = new Message(data, false);
		// check for msg data to be a multiple of 16 bytes
		Assert.assertEquals("Data length is not a multiple of 16 bytes",
				0, msg.getData().length % 16); 
		// test for speed (should take about one second)
		Timer timer = new Timer(2000, true);
		Message encrypted = manager.sendAndReceiveData(msg, true, 1000);
		Assert.assertNotNull("Encryption response from DE1 timed out (>1000ms)", encrypted);
		Assert.assertFalse("Encryption not done in time (>2000ms)", timer.hasExpired());
		timer.restart();
		Message decrypted = manager.sendAndReceiveData(encrypted, false, 1000);
		Assert.assertNotNull("Decryption response from DE1 timed out (>1000ms)", decrypted);
		Assert.assertFalse("Decryption not done in time (>2000ms)", timer.hasExpired());
		// test for same message after encryption and decryption 
		Assert.assertArrayEquals("Messages not equal after encryption, decryption cycle!", 
				msg.getData(), decrypted.getData());
	}

}
