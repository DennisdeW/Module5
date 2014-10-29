package soc.controller;

import global.Logger;
import global.Timer;
import global.Tools;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import soc.model.GPIOSettings;
import soc.model.Message;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;

/**
 * Represents the connection between the Pi and the DE1 SoC.<br>
 * It can be used to encrypt or decrypt data and in the future also for
 * compressing the data.
 * 
 * @author rvemous
 *
 */
public class GPIO {
    
	private static ReentrantLock lock;
	private static GpioController GPIO;
	
	private GPIOSettings settings;
	
	/**
	 * Creates a new gpio connection.
	 *  
	 * @param settings the settings to use
	 */
	public GPIO(GPIOSettings settings) {
		if (lock == null) {
			lock = new ReentrantLock();
		}
		if (GPIO == null) {
			Tools.waitForMs(100);
			System.out.println(Spi.wiringPiSPISetup(0, 500000));
			Tools.waitForMs(100);
			GPIO = GpioFactory.getInstance();
		}
		this.settings = settings;
	}

	/**
	 * Gets the gpio settings.
	 * 
	 * @return the settings
	 */
	public GPIOSettings getSettings() {
		return settings;
	}
	
	/**
	 * Sets the gpio settings.
	 * 
	 * @param settings to use
	 */
	public synchronized void setSettings(GPIOSettings settings) {
		this.settings = settings;
	}	

	/**
	 * Sends the data to the DE1 SoC for encryption/decryption.
	 * 
	 * @param data to send
	 * @param encrypt whether the DE1 should encrypt or decrypt the data
	 * @param timeout the max time in ms to wait for the DE1 to answer
	 * @return false when the DE1 has not responded within 
	 * the time out period
	 */
	public synchronized Message sendAndReceiveData(Message msg, boolean encrypt, long timeout) {
		if (!getLock(timeout)) {
			return null;
		}
		boolean fail = false;
		int maxBuff = Message.PACKET_SIZE;
		// wait till DE1 is not sending
		System.out.println("waiting for DE1 not sending");
		waitForPin(settings.getDe1ActivePin(GPIO), false, timeout);
		System.out.println("done.");
		// set encryption/decryption command pin		
		if (encrypt) { 
			System.out.println("Setting command pin to 0");
			settings.getCommandPin(GPIO).low();		
		} else {
			System.out.println("Setting command pin to 1");
			settings.getCommandPin(GPIO).high();	
		}
		System.out.println("done.");
		// tell the DE1 the Pi is going to send
		System.out.println("Settings pi active pin to 1");
		settings.getPiActivePin(GPIO).high();
		System.out.println("done.");
		// send data to DE1 for encryption/decryption and receive data
		System.out.println("Sending message to DE1: " + msg.toString() + 
				" to be " + (encrypt ? "encrypted" : "decrypted"));
		int readIndex = 0;
		byte[] buffer = new byte[maxBuff];
		byte[] allZeros = new byte[maxBuff];
		byte[] receivedBytes = new byte[msg.getData().length];
		Arrays.fill(allZeros, (byte)0);
		Timer timer = new Timer(timeout, true);
		for (int i = 0; i < msg.getData().length; i += maxBuff) {
			System.arraycopy(msg.getData(), i, buffer, 0, maxBuff);
			System.out.println("Sending buffer " + i + " : " + Arrays.toString(buffer));
			// try to send data
			boolean sendSuccesfull = true;
			if (Spi.wiringPiSPIDataRW(0, buffer, maxBuff) == -1) {
				Logger.logError("Could not send data to DE1: " + Arrays.toString(buffer));
				System.out.println("Could not send data to DE1: " + Arrays.toString(buffer));
				i -= maxBuff;
				sendSuccesfull = false;
			}
			if (sendSuccesfull) {
				System.out.println("Done sending buffer " + i + " - " + (i + maxBuff));
				// check for response from DE1
				if (!Tools.allEqualTo(buffer, (byte)0) && settings.getDe1ActivePin(GPIO).isHigh()) {
					System.out.println("Got answer " + i + " from DE1: " + buffer);
					System.arraycopy(buffer, 0, receivedBytes, i, maxBuff);
					readIndex += maxBuff;
				}
			}
			// check for time-out
			if (timer.hasExpired()) {
				Logger.logError("Timed out" + Arrays.toString(buffer));	
				System.out.println("Timed out" + Arrays.toString(buffer));	
				// tell the DE1 the Pi is done sending
				System.out.println("Set pi active pin to 0");
				settings.getPiActivePin(GPIO).low();
		        System.out.println("Done.");
		        releaseLock();
		        return null;
			}
		}	
		System.out.println("Done sending message, waiting for all packets to be received");
		// receive pending packages from DE1
		timer.restart();
		for (; readIndex < msg.getData().length;) {	
			System.arraycopy(allZeros, 0, buffer, 0, maxBuff);
			Spi.wiringPiSPIDataRW(0, buffer, maxBuff);
			// check for response from DE1
			if (!Tools.allEqualTo(buffer, (byte)0) && settings.getDe1ActivePin(GPIO).isHigh()) {
				System.out.println("Got answer " + readIndex + " from DE1: " + buffer);
				System.arraycopy(buffer, 0, receivedBytes, readIndex, maxBuff);
				readIndex += maxBuff;
			} else if (settings.getDe1ActivePin(GPIO).isLow()) {
				Logger.logError("Did not get whole message from DE1: "
						+ "\nGood:\t" + Arrays.toString(msg.getData()) + "\nActual:\t" 
						+ Arrays.toString(receivedBytes));
				System.out.println("Did not get whole message from DE1: "
						+ "\nGood:\t" + Arrays.toString(msg.getData()) + "\nActual:\t" 
						+ Arrays.toString(receivedBytes));
		        fail = true;
		        break;
			} else if (timer.hasExpired()) {
				Logger.logError("Timed out" + Arrays.toString(buffer));	
				System.out.println("Timed out" + Arrays.toString(buffer));
		        fail = true;
		        break;
			}
		}
		// tell the DE1 the Pi is done sending
		System.out.println("Set pi active pin to 0");
		settings.getPiActivePin(GPIO).low();
        System.out.println("Done.");
        releaseLock();
        if (fail) {
        	return null;
        } else {
    		System.out.println("Message " + (encrypt ? "encryption " : "decryption ") + "done");
        	return new Message(receivedBytes, msg.getActualSize(), encrypt);
        }
	}	
	
	/**
	 * Waits for the value of the specified pin to turn high or low.
	 * 
	 * @param pin to read
	 * @param waitforHigh whether to wait for the pin to turn high or low
	 * @param timeout the max time to wait for the event
	 * @return true if the event has happened, false if the time-out has
	 * expired
	 */
	public boolean waitForPin(GpioPinDigitalInput pin, boolean waitforHigh, long timeout) {
        Timer timer = new Timer(timeout, true);
		while (waitforHigh ? !pin.isHigh() : !pin.isLow()) {
			Tools.waitForMs(10);
			if (timer.hasExpired()) {
				return false;
			}
		}	
		return false;
	}
	
	/**
	 * Tries to get the lock.<br>
	 * It returns only when the lock has been acquired or the
	 * time-out expires.
	 * 
	 * @param timeout the time-out to use
	 * @return whether the lock has been aqcuired
	 */
	private boolean getLock(long timeout) {
		boolean hasLock = false;
		Timer timer = new Timer(timeout, true);
		while (!hasLock && !timer.hasExpired()) {
			Tools.waitForMs(10);
			hasLock = lock.tryLock();
		}
		return hasLock;
	}
	
	/**
	 * Releases the lock if this thread is holding it.
	 */
	private void releaseLock() {
		if (lock.isHeldByCurrentThread()) {
			lock.unlock();
		}
	}
	
	/**
	 * Safely shuts down all GPIO services.
	 */
	public void shutdown() {
		settings.reset();
		GPIO.shutdown();
	}
	
	// for testing only
	public static void main(String[] args) {
		GPIO gpio = new GPIO(new GPIOSettings());
		byte[] data = new byte[]{0,1,2,3,4,5,6,7,8,9};
		Message msg = new Message(data, false);
		gpio.sendAndReceiveData(msg, true, 250);
		gpio.sendAndReceiveData(msg, false, 250);
		gpio.sendAndReceiveData(msg, true, 250);
		gpio.sendAndReceiveData(msg, false, 250);
		gpio.shutdown();
	}

}
